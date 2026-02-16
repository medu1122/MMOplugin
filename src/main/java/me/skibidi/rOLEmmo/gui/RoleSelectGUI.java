package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.util.GUIUtil;
import me.skibidi.rolemmo.util.RolemmoIcons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

        // TANKER role – icon từ resource pack
        List<String> tankerLore = new ArrayList<>();
        tankerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_INFO + "§lHệ Hộ Thể / Kim Cang");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên phòng thủ");
        tankerLore.add(GUIUtil.COLOR_SECONDARY + "và bảo vệ đồng đội");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để chọn role này!");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(20, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_TANKER, GUIUtil.createBoldRoleName(Role.TANKER), tankerLore));

        // DPS role – icon từ resource pack
        List<String> dpsLore = new ArrayList<>();
        dpsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_ERROR + "§lHệ Sát Phạt / Chiến Đạo");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên gây sát thương");
        dpsLore.add(GUIUtil.COLOR_SECONDARY + "và tiêu diệt kẻ thù");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_SUCCESS + "§lClick để chọn role này!");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(22, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_DPS, GUIUtil.createBoldRoleName(Role.DPS), dpsLore));

        // HEALER role – icon từ resource pack
        List<String> healerLore = new ArrayList<>();
        healerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_SUCCESS + "§lHệ Linh Lực / Thánh Đạo");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_SECONDARY + "§lRole chuyên hỗ trợ");
        healerLore.add(GUIUtil.COLOR_SECONDARY + "và hồi máu đồng đội");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_SUCCESS + "§lClick để chọn role này!");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(24, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_HEALER, GUIUtil.createBoldRoleName(Role.HEALER), healerLore));

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
