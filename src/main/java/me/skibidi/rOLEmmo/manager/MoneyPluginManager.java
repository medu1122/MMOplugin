package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manager để tương tác với MoneyPlugin API
 * Soft dependency - plugin vẫn hoạt động nếu MoneyPlugin không có
 */
public class MoneyPluginManager {

    private final ROLEmmo plugin;
    private Object coinsManager;
    private boolean enabled = false;
    private final Logger logger;

    public MoneyPluginManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        initialize();
    }

    private void initialize() {
        try {
            Plugin moneyPlugin = plugin.getServer().getPluginManager().getPlugin("moneyPlugin");
            if (moneyPlugin != null && moneyPlugin.isEnabled()) {
                // Lấy CoinsManager từ MoneyPlugin
                try {
                    coinsManager = moneyPlugin.getClass().getMethod("getCoinsManager").invoke(moneyPlugin);
                    enabled = true;
                    logger.info("MoneyPlugin API initialized successfully!");
                } catch (Exception e) {
                    logger.warning("Failed to get CoinsManager from MoneyPlugin: " + e.getMessage());
                }
            } else {
                logger.info("MoneyPlugin not found. Role change cost feature will be disabled.");
            }
        } catch (Exception e) {
            logger.warning("Failed to initialize MoneyPlugin API: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem MoneyPlugin có sẵn không
     */
    public boolean isEnabled() {
        return enabled && coinsManager != null;
    }

    /**
     * Lấy số coins của player
     */
    public long getCoins(Player player) {
        if (!isEnabled()) return 0;
        if (player == null || !player.isOnline()) {
            return 0;
        }

        try {
            return (Long) coinsManager.getClass().getMethod("getCoins", Player.class).invoke(coinsManager, player);
        } catch (Exception e) {
            logger.warning("Failed to get coins for player " + (player != null ? player.getName() : "null") + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy số coins của player bằng UUID
     */
    public long getCoins(UUID uuid) {
        if (!isEnabled()) return 0;

        try {
            return (Long) coinsManager.getClass().getMethod("getCoins", UUID.class).invoke(coinsManager, uuid);
        } catch (Exception e) {
            logger.warning("Failed to get coins for UUID " + uuid + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Kiểm tra player có đủ coins không
     */
    public boolean hasEnough(Player player, long amount) {
        if (!isEnabled()) return false;
        if (player == null || !player.isOnline()) return false;
        if (amount < 0) {
            logger.warning("Invalid amount for hasEnough: " + amount);
            return false;
        }
        return getCoins(player) >= amount;
    }

    /**
     * Trừ coins của player
     */
    public boolean removeCoins(Player player, long amount) {
        if (!isEnabled()) {
            logger.warning("Cannot remove coins: MoneyPlugin not available");
            return false;
        }
        if (player == null || !player.isOnline()) {
            logger.warning("Cannot remove coins: Player is null or offline");
            return false;
        }
        if (amount < 0) {
            logger.warning("Cannot remove negative coins: " + amount);
            return false;
        }

        try {
            Boolean result = (Boolean) coinsManager.getClass()
                    .getMethod("removeCoins", Player.class, long.class)
                    .invoke(coinsManager, player, amount);
            return result != null && result;
        } catch (Exception e) {
            logger.warning("Failed to remove coins for player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Thêm coins cho player
     */
    public boolean addCoins(Player player, long amount) {
        if (!isEnabled()) {
            logger.warning("Cannot add coins: MoneyPlugin not available");
            return false;
        }
        if (player == null || !player.isOnline()) {
            logger.warning("Cannot add coins: Player is null or offline");
            return false;
        }
        if (amount < 0) {
            logger.warning("Cannot add negative coins: " + amount);
            return false;
        }

        try {
            Boolean result = (Boolean) coinsManager.getClass()
                    .getMethod("addCoins", Player.class, long.class)
                    .invoke(coinsManager, player, amount);
            return result != null && result;
        } catch (Exception e) {
            logger.warning("Failed to add coins for player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
}
