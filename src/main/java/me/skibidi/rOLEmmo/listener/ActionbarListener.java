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

        // Lấy skill đã chọn
        String selectedSkillId = skillManager.getSelectedSkillId(player);
        if (selectedSkillId == null) {
            // Chưa chọn skill, không hiển thị gì
            String lastMessage = lastActionbarMessage.get(player.getUniqueId());
            if (lastMessage != null) {
                // Clear actionbar nếu đã có message trước đó
                player.sendActionBar(" ");
                lastActionbarMessage.remove(player.getUniqueId());
            }
            return;
        }

        Skill selectedSkill = skillManager.getSkill(selectedSkillId);
        if (selectedSkill == null) {
            return;
        }

        int level = skillManager.getPlayerSkillLevel(player, selectedSkillId);
        if (level < 1) {
            // Chưa học skill này
            return;
        }

        // Hiển thị cooldown hoặc ready status
        String message;
        if (skillManager.isOnCooldown(player, selectedSkillId)) {
            long remaining = skillManager.getCooldownRemaining(player, selectedSkillId);
            message = "§c" + selectedSkill.getName() + " §7Cooldown: §e" + remaining + "s";
        } else {
            message = "§a" + selectedSkill.getName() + " §7đã sẵn sàng";
        }

        // Chỉ update nếu message thay đổi
        String lastMessage = lastActionbarMessage.get(player.getUniqueId());
        if (message != null) {
            if (!message.equals(lastMessage)) {
                player.sendActionBar(message);
                lastActionbarMessage.put(player.getUniqueId(), message);
            }
        } else {
            // Clear actionbar nếu không có message
            if (lastMessage != null) {
                player.sendActionBar(" ");
                lastActionbarMessage.remove(player.getUniqueId());
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
