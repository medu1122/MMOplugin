package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI để hiển thị danh sách skills của role
 */
public class SkillListGUI {

    /**
     * Mở GUI skill list cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        var roleManager = plugin.getRoleManager();
        var skillManager = plugin.getSkillManager();

        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) {
            player.sendMessage("§cBạn chưa chọn role!");
            return;
        }

        List<Skill> skills = skillManager.getSkills(currentRole);
        if (skills.isEmpty()) {
            openEmpty(player, plugin);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§6Skills - " + currentRole.getFullDisplayName());

        // Info item ở slot 4
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Thông Tin Skills");
            List<String> lore = new ArrayList<>();
            lore.add("§7Role: " + currentRole.getFullDisplayName());
            lore.add("§7Tổng số skills: §e" + skills.size());
            lore.add("§7Skill Points: §e" + roleManager.getSkillPoints(player));
            lore.add("");
            lore.add("§eClick vào skill để xem chi tiết!");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Hiển thị skills
        int[] skillSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < skills.size() && i < skillSlots.length; i++) {
            Skill skill = skills.get(i);
            int slot = skillSlots[i];
            int currentLevel = skillManager.getPlayerSkillLevel(player, skill.getId());
            
            ItemStack skillItem = createSkillItem(skill, currentLevel, skillManager, roleManager, player);
            inv.setItem(slot, skillItem);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Quay lại Role Info");
            back.setItemMeta(backMeta);
        }
        inv.setItem(48, back);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        // Glass panes decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        // Fill empty slots
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                boolean isSkillSlot = false;
                for (int skillSlot : skillSlots) {
                    if (i == skillSlot) {
                        isSkillSlot = true;
                        break;
                    }
                }
                if (!isSkillSlot && i != 4 && i != 48 && i != 49) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Mở GUI khi chưa có skill nào
     */
    private static void openEmpty(Player player, ROLEmmo plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Skills");

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Chưa Có Skills");
            List<String> lore = new ArrayList<>();
            lore.add("§7Role của bạn chưa có skills!");
            lore.add("§7Skills sẽ được thêm sau.");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(22, infoItem);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        // Glass panes
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null && i != 22 && i != 49) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Tạo ItemStack cho skill
     */
    private static ItemStack createSkillItem(Skill skill, int currentLevel, SkillManager skillManager, 
                                            me.skibidi.rolemmo.manager.RoleManager roleManager, Player player) {
        Material material = getMaterialForSkill(skill);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + skill.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + skill.getDescription());
            lore.add("");
            lore.add("§7Level hiện tại: §e" + currentLevel + "§7/§e" + skill.getMaxLevel());
            
            if (currentLevel < skill.getMaxLevel()) {
                int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
                lore.add("§7Level tiếp theo cần: §e" + requiredPoints + " điểm");
            } else {
                lore.add("§a§lĐã đạt level tối đa!");
            }
            
            lore.add("");
            lore.add("§eClick để xem chi tiết và upgrade!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Lấy Material phù hợp cho skill
     */
    private static Material getMaterialForSkill(Skill skill) {
        return switch (skill.getRole()) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.BLAZE_ROD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }
}
