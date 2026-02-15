package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.LevelManager;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.manager.TitleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI chính để hiển thị thông tin role
 */
public class RoleInfoGUI {

    /**
     * Mở GUI role info cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        RoleManager roleManager = plugin.getRoleManager();
        LevelManager levelManager = plugin.getLevelManager();
        TitleManager titleManager = plugin.getTitleManager();

        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) {
            player.sendMessage("§cBạn chưa chọn role! Sử dụng /role select");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§6Role Info");

        // Role info ở center
        ItemStack roleItem = new ItemStack(getMaterialForRole(currentRole));
        ItemMeta roleMeta = roleItem.getItemMeta();
        if (roleMeta != null) {
            roleMeta.setDisplayName(currentRole.getFullDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Role hiện tại của bạn");
            lore.add("");
            
            int level = roleManager.getRoleLevel(player, currentRole);
            int exp = roleManager.getRoleExp(player, currentRole);
            int requiredExp = levelManager.getRequiredExpForNextLevel(player, currentRole);
            int skillPoints = roleManager.getSkillPoints(player);
            
            lore.add("§7Level: §e" + level + "§7/§e999");
            lore.add("§7Exp: §e" + exp + "§7/§e" + requiredExp);
            if (level < 999) {
                double progress = (double) exp / requiredExp * 100;
                lore.add("§7Tiến độ: §e" + String.format("%.1f", progress) + "%");
            }
            lore.add("");
            lore.add("§7Skill Points: §e" + skillPoints);
            
            // Active title
            Title activeTitle = titleManager.getActiveTitle(player);
            if (activeTitle != null) {
                lore.add("");
                lore.add("§7Danh hiệu: " + activeTitle.getDisplayName());
            }
            
            roleMeta.setLore(lore);
            roleItem.setItemMeta(roleMeta);
        }
        inv.setItem(22, roleItem);

        // Skills button
        ItemStack skillsButton = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta skillsMeta = skillsButton.getItemMeta();
        if (skillsMeta != null) {
            skillsMeta.setDisplayName("§eXem Skills");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click để xem tất cả skills");
            lore.add("§7của role " + currentRole.getDisplayName());
            skillsMeta.setLore(lore);
            skillsButton.setItemMeta(skillsMeta);
        }
        inv.setItem(29, skillsButton);

        // Titles button
        ItemStack titlesButton = new ItemStack(Material.NAME_TAG);
        ItemMeta titlesMeta = titlesButton.getItemMeta();
        if (titlesMeta != null) {
            titlesMeta.setDisplayName("§6Danh Hiệu");
            List<String> lore = new ArrayList<>();
            List<Title> unlockedTitles = titleManager.getUnlockedTitles(player);
            lore.add("§7Click để xem danh hiệu");
            lore.add("§7Đã unlock: §e" + unlockedTitles.size());
            if (titleManager.getActiveTitle(player) != null) {
                lore.add("§7Đang dùng: " + titleManager.getActiveTitle(player).getDisplayName());
            }
            titlesMeta.setLore(lore);
            titlesButton.setItemMeta(titlesMeta);
        }
        inv.setItem(31, titlesButton);

        // Change role button (nếu có thể)
        if (roleManager.canChangeRole(player)) {
            ItemStack changeRoleButton = new ItemStack(Material.ENDER_PEARL);
            ItemMeta changeMeta = changeRoleButton.getItemMeta();
            if (changeMeta != null) {
                changeMeta.setDisplayName("§eĐổi Role");
                List<String> lore = new ArrayList<>();
                if (roleManager.canChangeRoleForFree(player)) {
                    lore.add("§7Click để đổi role");
                    lore.add("§aCó thể đổi miễn phí!");
                } else {
                    long cost = plugin.getConfigManager().getRoleChangeCost();
                    lore.add("§7Click để đổi role");
                    lore.add("§7Cost: §e" + cost + " coins");
                    lore.add("§7Hoặc đợi: §e" + roleManager.getTimeUntilCanChange(player));
                }
                changeMeta.setLore(lore);
                changeRoleButton.setItemMeta(changeMeta);
            }
            inv.setItem(33, changeRoleButton);
        }

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
                if (i != 22 && i != 29 && i != 31 && i != 33 && i != 49) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Lấy Material phù hợp cho role
     */
    private static Material getMaterialForRole(Role role) {
        return switch (role) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.DIAMOND_SWORD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }
}
