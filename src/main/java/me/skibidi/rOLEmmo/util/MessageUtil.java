package me.skibidi.rolemmo.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Gửi thông báo chỉ hiển thị phía client (Action Bar), không spam chat chung.
 * Dùng cho feedback: give, upgrade, chọn role, chọn skill, v.v.
 */
public final class MessageUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private MessageUtil() {}

    /**
     * Gửi thông báo chỉ player nhìn thấy (Action Bar), không ghi vào chat.
     * Chuỗi có thể dùng mã màu legacy (§a, §c, ...).
     */
    public static void sendActionBar(Player player, String legacyMessage) {
        if (player == null || legacyMessage == null || !player.isOnline()) {
            return;
        }
        try {
            player.sendActionBar(LEGACY.deserialize(legacyMessage));
        } catch (Throwable t) {
            player.sendMessage(legacyMessage);
        }
    }
}
