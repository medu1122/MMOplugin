package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.LevelManager;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * Listener để convert exp nhân vật thành exp role
 */
public class ExperienceListener implements Listener {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;
    private final LevelManager levelManager;

    public ExperienceListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
        this.levelManager = plugin.getLevelManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        
        // Check player online
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Kiểm tra player có role không
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) {
            return; // Chưa có role, không convert exp
        }

        // Lấy exp thay đổi
        int expChange = event.getAmount();
        if (expChange <= 0) {
            return;
        }

        // Convert và thêm exp cho role
        // Note: Exp change event có thể trigger nhiều lần, nhưng LevelManager sẽ xử lý đúng với synchronized
        levelManager.convertAndAddExperience(player, currentRole, expChange);
    }
}
