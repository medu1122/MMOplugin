package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import me.skibidi.rolemmo.util.SkillItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener để load/save player data khi join/quit
 */
public class PlayerDataListener implements Listener {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;
    private final SkillManager skillManager;

    public PlayerDataListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
        this.skillManager = plugin.getSkillManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data từ database (tự động load khi getPlayerRole được gọi)
        // Đảm bảo LuckPerms group được set đúng
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole != null) {
            if (roleManager.getLuckPermsManager().isEnabled()) {
                // Set lại LuckPerms group khi join (đảm bảo sync)
                roleManager.getLuckPermsManager().setPlayerGroup(player, currentRole);
            }
            
            // Give skill items sau 1 tick (đảm bảo inventory đã load)
            new BukkitRunnable() {
                @Override
                public void run() {
                    giveSkillItems(player, currentRole);
                    
                    // Đảm bảo có skill được chọn, nếu chưa có thì chọn skill đầu tiên
                    String selectedSkillId = skillManager.getSelectedSkillId(player);
                    if (selectedSkillId == null) {
                        var skills = skillManager.getSkills(currentRole);
                        if (!skills.isEmpty()) {
                            var firstSkill = skills.get(0);
                            int skillLevel = skillManager.getPlayerSkillLevel(player, firstSkill.getId());
                            if (skillLevel > 0) {
                                skillManager.selectSkill(player, firstSkill.getId());
                            }
                        }
                    } else {
                        // Đảm bảo skill item được give
                        var selectedSkill = skillManager.getSkill(selectedSkillId);
                        if (selectedSkill != null) {
                            int skillLevel = skillManager.getPlayerSkillLevel(player, selectedSkillId);
                            if (skillLevel > 0) {
                                me.skibidi.rolemmo.util.SkillItemUtil.ensureSkillItem(player, selectedSkill, skillLevel);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear cooldowns
        if (skillManager != null) {
            skillManager.clearCooldowns(player);
        }
        
        // Data sẽ được save tự động khi có thay đổi
        plugin.getLogger().fine("Player " + player.getName() + " quit - data saved");
    }

    /**
     * Give skill items cho player dựa trên role
     */
    private void giveSkillItems(Player player, Role role) {
        if (skillManager == null) {
            return;
        }

        var skills = skillManager.getSkills(role);
        for (Skill skill : skills) {
            int level = skillManager.getPlayerSkillLevel(player, skill.getId());
            if (level > 0) {
                SkillItemUtil.ensureSkillItem(player, skill, level);
            }
        }
    }
}
