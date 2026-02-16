package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import me.skibidi.rolemmo.skill.FireballSkill;
import me.skibidi.rolemmo.storage.DatabaseManager;
import me.skibidi.rolemmo.storage.repository.SkillRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manager để quản lý Skill System
 */
public class SkillManager {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private final SkillRepository skillRepository;
    private final RoleManager roleManager;
    private final me.skibidi.rolemmo.storage.repository.PlayerRoleRepository playerRoleRepository;
    private final Logger logger;

    // Cache skills (sử dụng ConcurrentHashMap để thread-safe, mặc dù chỉ write trong constructor)
    private final Map<Role, List<Skill>> skillsByRole = new ConcurrentHashMap<>();
    
    // Cooldown tracking (UUID -> skillId -> cooldown end time)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public SkillManager(ROLEmmo plugin, RoleManager roleManager) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.skillRepository = new SkillRepository(plugin.getDatabaseManager());
        this.roleManager = roleManager;
        this.playerRoleRepository = new me.skibidi.rolemmo.storage.repository.PlayerRoleRepository(plugin.getDatabaseManager());
        this.logger = plugin.getLogger();
        initializeSkills();
    }

    /**
     * Khởi tạo skills cho từng role
     */
    private void initializeSkills() {
        // DPS Skills
        List<Skill> dpsSkills = new ArrayList<>();
        dpsSkills.add(new FireballSkill(plugin));
        skillsByRole.put(Role.DPS, dpsSkills);

        // TANKER Skills (placeholder - sẽ thêm sau)
        skillsByRole.put(Role.TANKER, new ArrayList<>());

        // HEALER Skills (placeholder - sẽ thêm sau)
        skillsByRole.put(Role.HEALER, new ArrayList<>());

        logger.info("Initialized skills: DPS=" + dpsSkills.size() + ", TANKER=0, HEALER=0");
    }

    /**
     * Lấy tất cả skills của role
     */
    public List<Skill> getSkills(Role role) {
        return skillsByRole.getOrDefault(role, new ArrayList<>());
    }

    /**
     * Lấy skill theo ID
     */
    public Skill getSkill(String skillId) {
        for (List<Skill> skills : skillsByRole.values()) {
            for (Skill skill : skills) {
                if (skill.getId().equals(skillId)) {
                    return skill;
                }
            }
        }
        return null;
    }

    /**
     * Lấy skill level của player
     */
    public int getPlayerSkillLevel(Player player, String skillId) {
        try {
            return skillRepository.getSkillLevel(player.getUniqueId(), skillId);
        } catch (SQLException e) {
            logger.warning("Failed to get skill level for " + player.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Đảm bảo mỗi skill của role có ít nhất level 1 (để player có thể dùng skill ngay).
     * Gọi khi give skill items hoặc khi player có role.
     */
    public void ensureDefaultSkillLevels(Player player, Role role) {
        if (player == null || role == null) return;
        List<Skill> skills = getSkills(role);
        if (skills == null) return;
        for (Skill skill : skills) {
            int level = getPlayerSkillLevel(player, skill.getId());
            if (level < 1) {
                try {
                    skillRepository.setSkillLevel(player.getUniqueId(), skill.getId(), 1);
                    logger.fine("Set default level 1 for skill " + skill.getId() + " for " + player.getName());
                } catch (SQLException e) {
                    logger.warning("Failed to init skill level for " + skill.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Upgrade skill cho player
     * Synchronized để tránh race condition khi upgrade cùng lúc
     */
    public synchronized boolean upgradeSkill(Player player, String skillId) {
        if (player == null || skillId == null) {
            return false;
        }

        Skill skill = getSkill(skillId);
        if (skill == null) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cSkill không tồn tại!");
            return false;
        }

        int currentLevel = getPlayerSkillLevel(player, skillId);
        if (currentLevel >= skill.getMaxLevel()) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessage("skill_max_level"));
            return false;
        }

        // Check skill points
        int requiredPoints = configManager.getSkillUpgradeCost(currentLevel);
        int playerPoints = roleManager.getSkillPoints(player);

        if (playerPoints < requiredPoints) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessage("skill_not_enough_points"));
            return false;
        }

        try {
            // Double check skill points trước khi upgrade (tránh race condition)
            // Note: addSkillPoints đã synchronized, nhưng vẫn double check để đảm bảo
            int actualCurrentPoints = roleManager.getSkillPoints(player);
            if (actualCurrentPoints < requiredPoints) {
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessage("skill_not_enough_points"));
                return false;
            }
            
            // Upgrade skill
            int newLevel = currentLevel + 1;
            skillRepository.setSkillLevel(player.getUniqueId(), skillId, newLevel);

            // Trừ skill points (sử dụng addSkillPoints để đảm bảo consistency và validation)
            // addSkillPoints đã synchronized, nên sẽ thread-safe
            roleManager.addSkillPoints(player, -requiredPoints);

            // Update skill item nếu đang được sử dụng
            if (player.isOnline()) {
                me.skibidi.rolemmo.util.SkillItemUtil.ensureSkillItem(player, skill, newLevel);
            }

            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessage("skill_upgraded")
                    .replace("{skill}", skill.getName())
                    .replace("{level}", String.valueOf(newLevel)));

            logger.info("Player " + player.getName() + " upgraded skill " + skillId + " to level " + newLevel);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to upgrade skill for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cLỗi khi upgrade skill! Vui lòng thử lại sau.");
            return false;
        }
    }

    /**
     * Execute skill cho player
     */
    public boolean executeSkill(Player player, String skillId) {
        if (player == null || skillId == null) {
            return false;
        }

        // Check player online
        if (!player.isOnline()) {
            return false;
        }

        Skill skill = getSkill(skillId);
        if (skill == null) {
            return false;
        }

        // Check role match (quan trọng: đảm bảo skill thuộc role hiện tại)
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null || skill.getRole() != currentRole) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cSkill này không thuộc role hiện tại của bạn!");
            return false;
        }

        // Check cooldown
        if (isOnCooldown(player, skillId)) {
            long remaining = getCooldownRemaining(player, skillId);
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cSkill đang cooldown! Còn lại: " + remaining + "s");
            return false;
        }

        int level = getPlayerSkillLevel(player, skillId);
        if (level < 1) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cBạn chưa học skill này!");
            return false;
        }

        // Execute skill với error handling
        boolean success = false;
        try {
            success = skill.execute(player, level);
            if (success) {
                // Set cooldown (check null cho levelInfo)
                Skill.SkillLevelInfo levelInfo = skill.getLevelInfo(level);
                if (levelInfo != null) {
                    setCooldown(player, skillId, levelInfo.getCooldown());
                } else {
                    logger.warning("LevelInfo is null for skill " + skillId + " level " + level);
                }
            }
        } catch (Exception e) {
            // Sử dụng ErrorHandler để xử lý lỗi nhất quán
            me.skibidi.rolemmo.util.ErrorHandler.handleSkillError(player, skillId, e);
            return false;
        }

        return success;
    }

    /**
     * Check xem skill có đang cooldown không
     */
    public boolean isOnCooldown(Player player, String skillId) {
        if (player == null || skillId == null) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) {
            return false;
        }

        // Clean up expired cooldowns
        if (System.currentTimeMillis() >= cooldownEnd) {
            playerCooldowns.remove(skillId);
            // Clean up empty map để tránh memory leak
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUniqueId());
            }
            return false;
        }

        return true;
    }

    /**
     * Lấy thời gian cooldown còn lại (seconds)
     */
    public long getCooldownRemaining(Player player, String skillId) {
        if (player == null || skillId == null) {
            return 0;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }

        Long cooldownEnd = playerCooldowns.get(skillId);
        if (cooldownEnd == null) {
            return 0;
        }

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            // Clean up expired cooldown
            playerCooldowns.remove(skillId);
            // Clean up empty map để tránh memory leak
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUniqueId());
            }
            return 0;
        }

        return remaining;
    }

    /**
     * Set cooldown cho skill
     */
    private void setCooldown(Player player, String skillId, int cooldownSeconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(skillId, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    /**
     * Clear cooldown khi player quit
     */
    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Lấy tất cả skill levels của player
     */
    public Map<String, Integer> getPlayerSkills(Player player) {
        try {
            return skillRepository.getPlayerSkills(player.getUniqueId());
        } catch (SQLException e) {
            logger.warning("Failed to get player skills: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Lấy skill ID đang được chọn của player
     */
    public String getSelectedSkillId(Player player) {
        try {
            var data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getSelectedSkillId() : null;
        } catch (SQLException e) {
            logger.warning("Failed to get selected skill: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy thời gian lần cuối đổi skill
     */
    public long getLastSkillChange(Player player) {
        try {
            var data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getLastSkillChange() : 0;
        } catch (SQLException e) {
            logger.warning("Failed to get last skill change: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Chọn skill cho player (cooldown 30 phút)
     * Synchronized để tránh race condition khi chọn skill cùng lúc
     */
    public synchronized boolean selectSkill(Player player, String skillId) {
        if (player == null || skillId == null) {
            return false;
        }

        // Check player online
        if (!player.isOnline()) {
            return false;
        }

        Skill skill = getSkill(skillId);
        if (skill == null) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cSkill không tồn tại!");
            return false;
        }

        // Check role match
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null || skill.getRole() != currentRole) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cSkill này không thuộc role hiện tại của bạn!");
            return false;
        }

        // Check cooldown (30 phút)
        long lastChange = getLastSkillChange(player);
        long timeSinceChange = System.currentTimeMillis() - lastChange;
        long cooldownMs = 30 * 60 * 1000L; // 30 phút

        if (timeSinceChange < cooldownMs) {
            long remainingMinutes = (cooldownMs - timeSinceChange) / (60 * 1000);
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cBạn cần đợi thêm §e" + remainingMinutes + " phút §cđể đổi skill! §7(" + formatTime(cooldownMs - timeSinceChange) + ")");
            return false;
        }

        // Check skill level
        int skillLevel = getPlayerSkillLevel(player, skillId);
        if (skillLevel < 1) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cBạn chưa học skill này! Hãy upgrade skill trước.");
            return false;
        }

        try {
            var data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data != null) {
                data.setSelectedSkillId(skillId);
                data.setLastSkillChange(System.currentTimeMillis());
                playerRoleRepository.savePlayerRole(data);

                // Update skill item (check player online trước)
                if (player.isOnline()) {
                    me.skibidi.rolemmo.util.SkillItemUtil.removeAllSkillItems(player, currentRole);
                    me.skibidi.rolemmo.util.SkillItemUtil.ensureSkillItem(player, skill, skillLevel);
                }

                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§aĐã chọn skill: §e" + skill.getName());
                logger.info("Player " + player.getName() + " selected skill: " + skillId);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Failed to select skill: " + e.getMessage());
            e.printStackTrace();
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cLỗi khi chọn skill! Vui lòng thử lại sau.");
        }

        return false;
    }

    /**
     * Format time (milliseconds) thành string
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
