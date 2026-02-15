package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.util.GUIUtil;
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

        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("CHỌN ROLE", GUIUtil.GRADIENT_GOLD));

        // TANKER role với font lớn và màu sắc đẹp
        ItemStack tankerItem = new ItemStack(Material.SHIELD);
        ItemMeta tankerMeta = tankerItem.getItemMeta();
        if (tankerMeta != null) {
            tankerMeta.setDisplayName(GUIUtil.createLargeTitle("🛡️ TANKER", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§lHệ Hộ Thể / Kim Cang");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên phòng thủ");
            lore.add(GUIUtil.COLOR_SECONDARY + "và bảo vệ đồng đội");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để chọn role này!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            tankerMeta.setLore(lore);
            tankerItem.setItemMeta(tankerMeta);
        }
        inv.setItem(20, tankerItem);

        // DPS role với font lớn và màu sắc đẹp
        ItemStack dpsItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta dpsMeta = dpsItem.getItemMeta();
        if (dpsMeta != null) {
            dpsMeta.setDisplayName(GUIUtil.createLargeTitle("⚔️ DPS", GUIUtil.GRADIENT_RED));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_ERROR + "§lHệ Sát Phạt / Chiến Đạo");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên gây sát thương");
            lore.add(GUIUtil.COLOR_SECONDARY + "và tiêu diệt kẻ thù");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để chọn role này!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            dpsMeta.setLore(lore);
            dpsItem.setItemMeta(dpsMeta);
        }
        inv.setItem(22, dpsItem);

        // HEALER role với font lớn và màu sắc đẹp
        ItemStack healerItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healerMeta = healerItem.getItemMeta();
        if (healerMeta != null) {
            healerMeta.setDisplayName(GUIUtil.createLargeTitle("✝️ HEALER", GUIUtil.GRADIENT_GREEN));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§lHệ Linh Lực / Thánh Đạo");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên hỗ trợ");
            lore.add(GUIUtil.COLOR_SECONDARY + "và hồi máu đồng đội");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để chọn role này!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            healerMeta.setLore(lore);
            healerItem.setItemMeta(healerMeta);
        }
        inv.setItem(24, healerItem);

        // Glass panes decoration với màu sắc đa dạng
        ItemStack glass = GUIUtil.createGlassPane("gray");

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
