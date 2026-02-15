package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener để hiển thị skill cooldown trên actionbar
 */
public class ActionbarListener {

    private final ROLEmmo plugin;
    private final SkillManager skillManager;
    private final Map<UUID, String> lastActionbarMessage = new HashMap<>();

    public ActionbarListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.skillManager = plugin.getSkillManager();
        startActionbarTask();
    }

    /**
     * Bắt đầu task để update actionbar mỗi tick
     */
    private void startActionbarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateActionbar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update mỗi giây (20 ticks)
    }

    /**
     * Update actionbar cho player
     */
    private void updateActionbar(Player player) {
        var roleManager = plugin.getRoleManager();
        Role currentRole = roleManager.getPlayerRole(player);
        
        if (currentRole == null) {
            return;
        }

        var skills = skillManager.getSkills(currentRole);
        if (skills.isEmpty()) {
            return;
        }

        // Tìm skill đang cooldown hoặc sẵn sàng
        String message = null;
        for (Skill skill : skills) {
            String skillId = skill.getId();
            int level = skillManager.getPlayerSkillLevel(player, skillId);
            
            if (level < 1) {
                continue; // Chưa học skill này
            }

            if (skillManager.isOnCooldown(player, skillId)) {
                long remaining = skillManager.getCooldownRemaining(player, skillId);
                message = "§c" + skill.getName() + " §7Cooldown: §e" + remaining + "s";
                break; // Ưu tiên hiển thị skill đang cooldown
            } else {
                if (message == null) {
                    message = "§a" + skill.getName() + " §7đã sẵn sàng";
                }
            }
        }

        // Chỉ update nếu message thay đổi
        String lastMessage = lastActionbarMessage.get(player.getUniqueId());
        if (!message.equals(lastMessage)) {
            if (message != null) {
                player.sendActionBar(message);
                lastActionbarMessage.put(player.getUniqueId(), message);
            }
        }
    }

    /**
     * Clear actionbar khi player quit
     */
    public void clearActionbar(Player player) {
        lastActionbarMessage.remove(player.getUniqueId());
    }
}
