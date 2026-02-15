package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Manager để tương tác với ClanCore API
 * Soft dependency - plugin vẫn hoạt động nếu ClanCore không có
 */
public class ClanCoreManager {

    private final ROLEmmo plugin;
    private Object teamManager;
    private boolean enabled = false;
    private final Logger logger;

    public ClanCoreManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        initialize();
    }

    private void initialize() {
        try {
            Plugin clanCore = plugin.getServer().getPluginManager().getPlugin("ClanCore");
            if (clanCore != null && clanCore.isEnabled()) {
                // Lấy TeamManager từ ClanCore bằng reflection
                try {
                    // Thử lấy từ field teamManager trong main class
                    java.lang.reflect.Field field = clanCore.getClass().getDeclaredField("teamManager");
                    field.setAccessible(true);
                    teamManager = field.get(clanCore);
                    
                    if (teamManager != null) {
                        enabled = true;
                        logger.info("ClanCore API initialized successfully!");
                    }
                } catch (NoSuchFieldException e) {
                    // Thử cách khác: tìm trong superclass hoặc check method
                    try {
                        java.lang.reflect.Method method = clanCore.getClass().getMethod("getTeamManager");
                        teamManager = method.invoke(clanCore);
                        if (teamManager != null) {
                            enabled = true;
                            logger.info("ClanCore API initialized successfully!");
                        }
                    } catch (Exception e2) {
                        logger.warning("Failed to get TeamManager from ClanCore. Team protection will be disabled.");
                        logger.warning("Error: " + e2.getMessage());
                    }
                } catch (Exception e) {
                    logger.warning("Failed to access TeamManager from ClanCore: " + e.getMessage());
                }
            } else {
                logger.info("ClanCore not found. Team protection feature will be disabled.");
            }
        } catch (Exception e) {
            logger.warning("Failed to initialize ClanCore API: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem ClanCore có sẵn không
     */
    public boolean isEnabled() {
        return enabled && teamManager != null;
    }

    /**
     * Kiểm tra 2 players có cùng team không
     */
    public boolean areSameTeam(Player player1, Player player2) {
        if (!isEnabled()) return false;

        try {
            Boolean result = (Boolean) teamManager.getClass()
                    .getMethod("sameTeam", Player.class, Player.class)
                    .invoke(teamManager, player1, player2);
            return result != null && result;
        } catch (Exception e) {
            logger.warning("Failed to check same team: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra player có trong team không
     */
    public boolean isInTeam(Player player) {
        if (!isEnabled()) return false;

        try {
            Boolean result = (Boolean) teamManager.getClass()
                    .getMethod("isInTeam", Player.class)
                    .invoke(teamManager, player);
            return result != null && result;
        } catch (Exception e) {
            logger.warning("Failed to check if player in team: " + e.getMessage());
            return false;
        }
    }
}
