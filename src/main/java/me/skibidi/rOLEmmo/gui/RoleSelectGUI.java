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
 * GUI để chọn role lần đầu
 */
public class RoleSelectGUI {

    /**
     * Mở GUI chọn role cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        RoleManager roleManager = plugin.getRoleManager();
        
        // Nếu đã có role, mở RoleInfoGUI thay vì RoleSelectGUI
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole != null) {
            RoleInfoGUI.open(player, plugin);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§6Chọn Role");

        // TANKER role
        ItemStack tankerItem = new ItemStack(Material.SHIELD);
        ItemMeta tankerMeta = tankerItem.getItemMeta();
        if (tankerMeta != null) {
            tankerMeta.setDisplayName("§6§lTANKER");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Hộ Thể / Kim Cang");
            lore.add("");
            lore.add("§7Role chuyên phòng thủ");
            lore.add("§7và bảo vệ đồng đội");
            lore.add("");
            lore.add("§eClick để chọn role này!");
            tankerMeta.setLore(lore);
            tankerItem.setItemMeta(tankerMeta);
        }
        inv.setItem(20, tankerItem);

        // DPS role
        ItemStack dpsItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta dpsMeta = dpsItem.getItemMeta();
        if (dpsMeta != null) {
            dpsMeta.setDisplayName("§c§lDPS");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Sát Phạt / Chiến Đạo");
            lore.add("");
            lore.add("§7Role chuyên gây sát thương");
            lore.add("§7và tiêu diệt kẻ thù");
            lore.add("");
            lore.add("§eClick để chọn role này!");
            dpsMeta.setLore(lore);
            dpsItem.setItemMeta(dpsMeta);
        }
        inv.setItem(22, dpsItem);

        // HEALER role
        ItemStack healerItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healerMeta = healerItem.getItemMeta();
        if (healerMeta != null) {
            healerMeta.setDisplayName("§a§lHEALER");
            List<String> lore = new ArrayList<>();
            lore.add("§7Hệ Linh Lực / Thánh Đạo");
            lore.add("");
            lore.add("§7Role chuyên hỗ trợ");
            lore.add("§7và hồi máu đồng đội");
            lore.add("");
            lore.add("§eClick để chọn role này!");
            healerMeta.setLore(lore);
            healerItem.setItemMeta(healerMeta);
        }
        inv.setItem(24, healerItem);

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
                if (i != 20 && i != 22 && i != 24) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }
}
