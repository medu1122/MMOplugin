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

    private static final NamespacedKey SKILL_ID_KEY = new NamespacedKey(ROLEmmo.getInstance(), "skill_id");
    private static final NamespacedKey SKILL_ITEM_KEY = new NamespacedKey(ROLEmmo.getInstance(), "is_skill_item");

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
            lore.add("§7Cooldown: §e" + skill.getLevelInfo(level).getCooldown() + "s");
            meta.setLore(lore);

            // Set persistent data để identify skill item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(SKILL_ITEM_KEY, PersistentDataType.BOOLEAN, true);
            container.set(SKILL_ID_KEY, PersistentDataType.STRING, skill.getId());

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
        Boolean isSkillItem = container.get(SKILL_ITEM_KEY, PersistentDataType.BOOLEAN);
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
        return container.get(SKILL_ID_KEY, PersistentDataType.STRING);
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

        ROLEmmo plugin = ROLEmmo.getInstance();
        if (plugin == null) {
            return;
        }

        var skillManager = plugin.getSkillManager();
        if (skillManager == null) {
            return;
        }

        List<Skill> skills = skillManager.getSkills(role);
        for (Skill skill : skills) {
            removeSkillItem(player, skill.getId());
        }
    }
}
