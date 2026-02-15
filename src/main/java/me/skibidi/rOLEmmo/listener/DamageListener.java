package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.ClanCoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Listener để check team protection trước khi damage
 * Đảm bảo skill không gây damage cho teammate
 */
public class DamageListener implements Listener {

    private final ROLEmmo plugin;

    public DamageListener(ROLEmmo plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Lấy ClanCoreManager từ RoleManager (reuse instance)
     */
    private ClanCoreManager getClanCoreManager() {
        var roleManager = plugin.getRoleManager();
        return roleManager != null ? roleManager.getClanCoreManager() : null;
    }

    /**
     * Check team protection khi có damage giữa players
     * Note: FireballSkill đã có team check, nhưng đây là backup để đảm bảo
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Chỉ xử lý khi cả attacker và victim đều là Player
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // Check players online
        if (!attacker.isOnline() || !victim.isOnline()) {
            return;
        }

        // Check nếu cùng team thì cancel damage
        ClanCoreManager clanCoreManager = getClanCoreManager();
        if (clanCoreManager != null && clanCoreManager.isEnabled()) {
            try {
                if (clanCoreManager.areSameTeam(attacker, victim)) {
                    event.setCancelled(true);
                    // Có thể thêm message nếu muốn
                    // attacker.sendMessage("§cKhông thể tấn công đồng đội!");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking team in DamageListener: " + e.getMessage());
            }
        }
    }
}
