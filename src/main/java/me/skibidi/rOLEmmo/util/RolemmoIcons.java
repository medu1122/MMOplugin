package me.skibidi.rolemmo.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Map icon ID → CustomModelData và tạo item dùng texture từ resource pack.
 * 1.21.4: dùng Data Component strings + items model select (assets/minecraft/items/paper.json).
 */
public final class RolemmoIcons {

    /** CustomModelData trong pack (paper items model select "1"–"10") */
    public static final int ICON_ROLE_DPS = 1;
    public static final int ICON_ROLE_TANKER = 2;
    public static final int ICON_ROLE_HEALER = 3;
    public static final int ICON_ROLE_LOGO = 4;
    public static final int ICON_BTN_CHANGE = 5;
    public static final int ICON_BTN_SKILLS = 6;
    public static final int ICON_BTN_SELECT_SKILL = 7;
    public static final int ICON_BTN_TITLES = 8;
    public static final int ICON_BTN_CLOSE = 9;
    public static final int ICON_SKILL_FIREBALL = 10;

    private RolemmoIcons() {}

    /**
     * Tạo item Paper có CustomModelData (strings "1"–"10") để hiện icon từ resource pack 1.21.4.
     * Nếu client không có pack thì vẫn thấy item Paper bình thường.
     */
    public static ItemStack createIcon(int customModelData, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addString(String.valueOf(customModelData)).build());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Chỉ set CustomModelData (string) lên item có sẵn (giữ nguyên name/lore).
     */
    public static void applyIcon(ItemStack item, int customModelData) {
        if (item == null || item.getType() != Material.PAPER) {
            return;
        }
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addString(String.valueOf(customModelData)).build());
    }

    /**
     * CustomModelData cho role (DPS=1, Tanker=2, Healer=3).
     */
    public static int getRoleIconId(me.skibidi.rolemmo.model.Role role) {
        if (role == null) return ICON_ROLE_LOGO;
        return switch (role) {
            case DPS -> ICON_ROLE_DPS;
            case TANKER -> ICON_ROLE_TANKER;
            case HEALER -> ICON_ROLE_HEALER;
        };
    }
}
