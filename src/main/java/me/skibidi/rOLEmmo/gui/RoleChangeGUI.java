package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI để đổi role
 */
public class RoleChangeGUI {

    /**
     * Mở GUI đổi role cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        RoleManager roleManager = plugin.getRoleManager();
        Role currentRole = roleManager.getPlayerRole(player);
        
        if (currentRole == null) {
            // Chưa có role, mở RoleSelectGUI
            RoleSelectGUI.open(player, plugin);
            return;
        }

        boolean canChange = roleManager.canChangeRole(player);
        boolean canChangeForFree = roleManager.canChangeRoleForFree(player);
        long cost = plugin.getConfigManager().getRoleChangeCost();
        String timeUntilCanChange = roleManager.getTimeUntilCanChange(player);

        Inventory inv = Bukkit.createInventory(null, 54, "§6Đổi Role");

        // Info item ở center
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Thông Tin Đổi Role");
            List<String> lore = new ArrayList<>();
            lore.add("§7Role hiện tại: " + currentRole.getFullDisplayName());
            lore.add("");
            if (canChangeForFree) {
                lore.add("§aCó thể đổi role miễn phí!");
            } else if (canChange) {
                lore.add("§7Cost để đổi ngay: §e" + cost + " coins");
                lore.add("§7Hoặc đợi: §e" + timeUntilCanChange);
            } else {
                lore.add("§cChưa thể đổi role!");
                lore.add("§7Cần đợi: §e" + timeUntilCanChange);
                lore.add("§7Hoặc trả: §e" + cost + " coins");
            }
            lore.add("");
            lore.add("§eChọn role bên dưới để đổi!");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(22, infoItem);

        // TANKER role
        ItemStack tankerItem = new ItemStack(Material.SHIELD);
        ItemMeta tankerMeta = tankerItem.getItemMeta();
        if (tankerMeta != null) {
            boolean isCurrent = currentRole == Role.TANKER;
            tankerMeta.setDisplayName(isCurrent ? "§6§lTANKER §7(Hiện tại)" : "§6§lTANKER");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Hộ Thể / Kim Cang");
            lore.add("");
            if (isCurrent) {
                lore.add("§cĐây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add("§eClick để đổi sang role này!");
            } else {
                lore.add("§cChưa thể đổi role!");
            }
            tankerMeta.setLore(lore);
            tankerItem.setItemMeta(tankerMeta);
        }
        inv.setItem(20, tankerItem);

        // DPS role
        ItemStack dpsItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta dpsMeta = dpsItem.getItemMeta();
        if (dpsMeta != null) {
            boolean isCurrent = currentRole == Role.DPS;
            dpsMeta.setDisplayName(isCurrent ? "§c§lDPS §7(Hiện tại)" : "§c§lDPS");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Sát Phạt / Chiến Đạo");
            lore.add("");
            if (isCurrent) {
                lore.add("§cĐây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add("§eClick để đổi sang role này!");
            } else {
                lore.add("§cChưa thể đổi role!");
            }
            dpsMeta.setLore(lore);
            dpsItem.setItemMeta(dpsMeta);
        }
        inv.setItem(22, dpsItem); // Override info item

        // HEALER role
        ItemStack healerItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healerMeta = healerItem.getItemMeta();
        if (healerMeta != null) {
            boolean isCurrent = currentRole == Role.HEALER;
            healerMeta.setDisplayName(isCurrent ? "§a§lHEALER §7(Hiện tại)" : "§a§lHEALER");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Linh Lực / Thánh Đạo");
            lore.add("");
            if (isCurrent) {
                lore.add("§cĐây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add("§eClick để đổi sang role này!");
            } else {
                lore.add("§cChưa thể đổi role!");
            }
            healerMeta.setLore(lore);
            healerItem.setItemMeta(healerMeta);
        }
        inv.setItem(24, healerItem);

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
                if (i != 20 && i != 22 && i != 24 && i != 48 && i != 49) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }
}
