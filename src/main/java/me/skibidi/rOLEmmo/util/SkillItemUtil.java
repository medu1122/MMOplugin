package me.skibidi.rolemmo.util;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để tạo và quản lý skill items
 * Skill items không thể drop, không thể move, không rơi khi chết
 */
public class SkillItemUtil {

    private static NamespacedKey SKILL_ID_KEY;
    private static NamespacedKey SKILL_ITEM_KEY;
    
    /**
     * Initialize keys khi plugin đã enable
     */
    public static void initializeKeys() {
        ROLEmmo plugin = ROLEmmo.getInstance();
        if (plugin != null && plugin.isEnabled()) {
            SKILL_ID_KEY = new NamespacedKey(plugin, "skill_id");
            SKILL_ITEM_KEY = new NamespacedKey(plugin, "is_skill_item");
        }
    }
    
    /**
     * Get SKILL_ID_KEY với lazy initialization
     */
    private static NamespacedKey getSkillIdKey() {
        if (SKILL_ID_KEY == null) {
            initializeKeys();
        }
        if (SKILL_ID_KEY == null) {
            throw new IllegalStateException("SkillItemUtil keys not initialized. Plugin may not be enabled.");
        }
        return SKILL_ID_KEY;
    }
    
    /**
     * Get SKILL_ITEM_KEY với lazy initialization
     */
    private static NamespacedKey getSkillItemKey() {
        if (SKILL_ITEM_KEY == null) {
            initializeKeys();
        }
        if (SKILL_ITEM_KEY == null) {
            throw new IllegalStateException("SkillItemUtil keys not initialized. Plugin may not be enabled.");
        }
        return SKILL_ITEM_KEY;
    }

    /**
     * Tạo skill item cho skill
     */
    public static ItemStack createSkillItem(Skill skill, int level) {
        Material material = getMaterialForSkill(skill);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + skill.getName() + " §7(Lv." + level + ")");
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Skill: " + skill.getName());
            lore.add("§7Level: §e" + level);
            lore.add("");
            lore.add("§eChuột phải để sử dụng!");
            
            Skill.SkillLevelInfo levelInfo = skill.getLevelInfo(level);
            if (levelInfo != null) {
                lore.add("§7Cooldown: §e" + levelInfo.getCooldown() + "s");
            }
            meta.setLore(lore);

            // Set persistent data để identify skill item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(getSkillItemKey(), PersistentDataType.BOOLEAN, true);
            container.set(getSkillIdKey(), PersistentDataType.STRING, skill.getId());

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Kiểm tra xem item có phải skill item không
     */
    public static boolean isSkillItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Boolean isSkillItem = container.get(getSkillItemKey(), PersistentDataType.BOOLEAN);
        return isSkillItem != null && isSkillItem;
    }

    /**
     * Lấy skill ID từ item
     */
    public static String getSkillId(ItemStack item) {
        if (!isSkillItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(getSkillIdKey(), PersistentDataType.STRING);
    }

    /**
     * Lấy Material phù hợp cho skill
     */
    private static Material getMaterialForSkill(Skill skill) {
        // Có thể config sau, tạm thời dùng material theo role
        return switch (skill.getRole()) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.BLAZE_ROD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }

    /**
     * Đảm bảo player có skill item (thêm nếu chưa có, update nếu đã có)
     */
    public static void ensureSkillItem(Player player, Skill skill, int level) {
        if (player == null || skill == null) {
            return;
        }

        String skillId = skill.getId();
        ItemStack skillItem = createSkillItem(skill, level);

        // Tìm xem đã có skill item này chưa
        boolean found = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isSkillItem(item) && skillId.equals(getSkillId(item))) {
                // Update item
                player.getInventory().setItem(i, skillItem);
                found = true;
                break;
            }
        }

        // Nếu chưa có, thêm vào inventory (tìm slot trống)
        if (!found) {
            // Đảm bảo chỉ có 1 skill item
            removeSkillItem(player, skillId);
            
            int emptySlot = player.getInventory().firstEmpty();
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, skillItem);
            } else {
                // Inventory đầy, thông báo
                player.sendMessage("§cTúi đồ đầy! Không thể thêm skill item.");
            }
        }
    }

    /**
     * Remove skill item khỏi inventory
     */
    public static void removeSkillItem(Player player, String skillId) {
        if (player == null || skillId == null) {
            return;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isSkillItem(item) && skillId.equals(getSkillId(item))) {
                player.getInventory().setItem(i, null);
                break;
            }
        }
    }

    /**
     * Remove tất cả skill items của role
     */
    public static void removeAllSkillItems(Player player, Role role) {
        if (player == null || role == null) {
            return;
        }

        try {
            ROLEmmo plugin = ROLEmmo.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                return;
            }

            var skillManager = plugin.getSkillManager();
            if (skillManager == null) {
                return;
            }

            List<Skill> skills = skillManager.getSkills(role);
            if (skills == null) {
                return;
            }
            
            for (Skill skill : skills) {
                if (skill != null) {
                    removeSkillItem(player, skill.getId());
                }
            }
        } catch (Exception e) {
            // Plugin có thể đã disable, ignore
        }
    }
}
