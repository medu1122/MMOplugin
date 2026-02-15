package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.storage.DatabaseManager;
import me.skibidi.rolemmo.storage.repository.PlayerRoleRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Manager để quản lý Level & Experience System
 * Xử lý level up, exp conversion, và unlock titles
 */
public class LevelManager {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private final PlayerRoleRepository playerRoleRepository;
    private final TitleManager titleManager;
    private final RoleManager roleManager;
    private final Logger logger;

    public LevelManager(ROLEmmo plugin, TitleManager titleManager, RoleManager roleManager) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerRoleRepository = new PlayerRoleRepository(plugin.getDatabaseManager());
        this.titleManager = titleManager;
        this.roleManager = roleManager;
        this.logger = plugin.getLogger();
    }

    /**
     * Thêm exp cho role của player
     */
    public void addExperience(Player player, Role role, int exp) {
        if (player == null || role == null || exp <= 0) {
            return;
        }

        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) {
                logger.warning("Player data not found for " + player.getName() + " when adding exp");
                return;
            }

            int currentExp = data.getExp(role);
            int currentLevel = data.getLevel(role);
            int newExp = currentExp + exp;

            // Check level up
            int requiredExp = configManager.getRequiredExpForLevel(currentLevel);
            while (newExp >= requiredExp && currentLevel < 999) {
                // Level up!
                newExp -= requiredExp;
                currentLevel++;
                
                // Thêm skill point khi level up
                data.setSkillPoints(data.getSkillPoints() + 1);
                
                // Unlock titles nếu có
                titleManager.checkAndUnlockTitles(player, role, currentLevel);
                
                // Thông báo level up
                player.sendMessage(configManager.getMessage("level_up")
                        .replace("{level}", String.valueOf(currentLevel)));
                
                logger.info("Player " + player.getName() + " leveled up " + role.name() + " to level " + currentLevel);
                
                // Tính exp cần cho level tiếp theo
                requiredExp = configManager.getRequiredExpForLevel(currentLevel);
            }

            // Đảm bảo không vượt quá max level
            if (currentLevel >= 999) {
                currentLevel = 999;
                newExp = 0; // Reset exp khi đạt max level
            }

            // Update data
            data.setLevel(role, currentLevel);
            data.setExp(role, newExp);

            // Lưu vào database
            playerRoleRepository.savePlayerRole(data);

        } catch (SQLException e) {
            logger.severe("Failed to add experience for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error in addExperience: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convert exp nhân vật thành exp role và thêm vào
     */
    public void convertAndAddExperience(Player player, Role role, int playerExp) {
        if (player == null || role == null || playerExp <= 0) {
            return;
        }

        double conversionRate = configManager.getExpConversionRate();
        int roleExp = (int) (playerExp * conversionRate);
        
        if (roleExp > 0) {
            addExperience(player, role, roleExp);
        }
    }

    /**
     * Lấy exp hiện tại của role
     */
    public int getExperience(Player player, Role role) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getExp(role) : 0;
        } catch (SQLException e) {
            logger.warning("Failed to get experience: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy exp cần để lên level tiếp theo
     */
    public int getRequiredExpForNextLevel(Player player, Role role) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) {
                return configManager.getRequiredExpForLevel(1);
            }
            
            int currentLevel = data.getLevel(role);
            if (currentLevel >= 999) {
                return 0; // Đã max level
            }
            
            return configManager.getRequiredExpForLevel(currentLevel);
        } catch (SQLException e) {
            logger.warning("Failed to get required exp: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Set level cho player (admin command)
     */
    public boolean setLevel(Player player, Role role, int level) {
        if (player == null || role == null) {
            return false;
        }

        if (level < 1 || level > 999) {
            logger.warning("Invalid level: " + level + " (must be 1-999)");
            return false;
        }

        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) {
                logger.warning("Player data not found for " + player.getName());
                return false;
            }

            int oldLevel = data.getLevel(role);
            data.setLevel(role, level);
            data.setExp(role, 0); // Reset exp khi set level

            // Unlock titles cho level mới
            titleManager.checkAndUnlockTitles(player, role, level);

            // Lưu vào database
            playerRoleRepository.savePlayerRole(data);

            logger.info("Admin set level " + level + " for " + role.name() + " role of player " + player.getName());
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to set level: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
