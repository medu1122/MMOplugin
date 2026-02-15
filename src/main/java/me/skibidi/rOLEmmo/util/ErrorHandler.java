package me.skibidi.rolemmo.util;

import me.skibidi.rolemmo.ROLEmmo;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class để xử lý errors một cách nhất quán
 */
public class ErrorHandler {

    private static Logger getLogger() {
        ROLEmmo instance = ROLEmmo.getInstance();
        if (instance != null) {
            return instance.getLogger();
        }
        // Fallback logger nếu plugin chưa enable
        return java.util.logging.Logger.getLogger("ROLEmmo");
    }

    /**
     * Log error với thông tin chi tiết
     */
    public static void logError(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, message, throwable);
    }

    /**
     * Log warning
     */
    public static void logWarning(String message) {
        getLogger().warning(message);
    }

    /**
     * Log info
     */
    public static void logInfo(String message) {
        getLogger().info(message);
    }

    /**
     * Send error message cho player
     */
    public static void sendError(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage("§c[Lỗi] " + message);
        }
    }

    /**
     * Send warning message cho player
     */
    public static void sendWarning(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage("§e[Cảnh báo] " + message);
        }
    }

    /**
     * Handle database error
     */
    public static void handleDatabaseError(String operation, Throwable throwable) {
        logError("Database error during " + operation + ": " + throwable.getMessage(), throwable);
    }

    /**
     * Handle skill execution error
     */
    public static void handleSkillError(Player player, String skillId, Throwable throwable) {
        if (throwable == null) {
            logWarning("handleSkillError called with null throwable for skill " + skillId);
            return;
        }
        logError("Error executing skill " + skillId + " for player " + 
                (player != null ? player.getName() : "null") + ": " + throwable.getMessage(), throwable);
        sendError(player, "Lỗi khi sử dụng skill! Vui lòng thử lại sau.");
    }
}
