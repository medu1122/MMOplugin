package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener để load/save player data khi join/quit
 */
public class PlayerDataListener implements Listener {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;

    public PlayerDataListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data từ database (tự động load khi getPlayerRole được gọi)
        // Đảm bảo LuckPerms group được set đúng
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole != null && roleManager.getLuckPermsManager().isEnabled()) {
            // Set lại LuckPerms group khi join (đảm bảo sync)
            roleManager.getLuckPermsManager().setPlayerGroup(player, currentRole);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Data sẽ được save tự động khi có thay đổi
        // Có thể thêm logic save cuối cùng ở đây nếu cần
        plugin.getLogger().fine("Player " + player.getName() + " quit - data saved");
    }
}
