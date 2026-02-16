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

        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("DOI ROLE", GUIUtil.GRADIENT_PURPLE));

        // Info item ở center với font lớn
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(GUIUtil.createLargeTitle("THONG TIN", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            String[] roleGradient = getGradientForRole(currentRole);
            lore.add(GUIUtil.COLOR_INFO + "§lRole hiện tại: " + GUIUtil.createBoldRoleName(currentRole));
            lore.add(" ");
            if (canChangeForFree) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§lCó thể đổi role miễn phí!");
            } else if (canChange) {
                lore.add(GUIUtil.COLOR_WARNING + "§lCost để đổi ngay: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
                lore.add(GUIUtil.COLOR_MUTED + "Hoặc đợi: " + GUIUtil.COLOR_INFO + timeUntilCanChange);
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§lChưa thể đổi role!");
                lore.add(GUIUtil.COLOR_MUTED + "Cần đợi: " + GUIUtil.COLOR_INFO + timeUntilCanChange);
                lore.add(GUIUtil.COLOR_MUTED + "Hoặc trả: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§lChọn role bên dưới để đổi!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(22, infoItem);

        // TANKER role – icon từ pack
        boolean tankerCurrent = currentRole == Role.TANKER;
        List<String> tankerLore = new ArrayList<>();
        tankerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_INFO + "§lHệ Hộ Thể / Kim Cang");
        tankerLore.add(" ");
        tankerLore.add(tankerCurrent ? GUIUtil.COLOR_ERROR + "§l✖ Đây là role hiện tại của bạn!" : (canChange || canChangeForFree) ? GUIUtil.COLOR_SUCCESS + "§l✓ Click để đổi sang role này!" : GUIUtil.COLOR_ERROR + "§l✖ Chưa thể đổi role!");
        tankerLore.add(" ");
        tankerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(20, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_TANKER,
                (tankerCurrent ? GUIUtil.createBoldRoleName(Role.TANKER) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" : GUIUtil.createBoldRoleName(Role.TANKER)), tankerLore));

        // DPS role – icon từ pack (slot 22)
        boolean dpsCurrent = currentRole == Role.DPS;
        List<String> dpsLore = new ArrayList<>();
        dpsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_ERROR + "§lHệ Sát Phạt / Chiến Đạo");
        dpsLore.add(" ");
        dpsLore.add(dpsCurrent ? GUIUtil.COLOR_ERROR + "§lĐây là role hiện tại của bạn!" : (canChange || canChangeForFree) ? GUIUtil.COLOR_SUCCESS + "§lClick để đổi sang role này!" : GUIUtil.COLOR_ERROR + "§lChưa thể đổi role!");
        dpsLore.add(" ");
        dpsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(22, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_DPS,
                (dpsCurrent ? GUIUtil.createBoldRoleName(Role.DPS) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" : GUIUtil.createBoldRoleName(Role.DPS)), dpsLore));

        // HEALER role – icon từ pack
        boolean healerCurrent = currentRole == Role.HEALER;
        List<String> healerLore = new ArrayList<>();
        healerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_SUCCESS + "§lHệ Linh Lực / Thánh Đạo");
        healerLore.add(" ");
        healerLore.add(healerCurrent ? GUIUtil.COLOR_ERROR + "§lĐây là role hiện tại của bạn!" : (canChange || canChangeForFree) ? GUIUtil.COLOR_SUCCESS + "§lClick để đổi sang role này!" : GUIUtil.COLOR_ERROR + "§lChưa thể đổi role!");
        healerLore.add(" ");
        healerLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(24, RolemmoIcons.createIcon(RolemmoIcons.ICON_ROLE_HEALER,
                (healerCurrent ? GUIUtil.createBoldRoleName(Role.HEALER) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" : GUIUtil.createBoldRoleName(Role.HEALER)), healerLore));

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Quay lại Role Info");
            back.setItemMeta(backMeta);
        }
        inv.setItem(48, back);

        // Close button – icon từ pack
        inv.setItem(49, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CLOSE, "§cĐóng", null));

        // Glass panes decoration với màu sắc đa dạng
        ItemStack glass = GUIUtil.createGlassPane("gray");

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

    /**
     * Lấy gradient colors cho role
     */
    private static String[] getGradientForRole(Role role) {
        return switch (role) {
            case TANKER -> GUIUtil.GRADIENT_BLUE;
            case DPS -> GUIUtil.GRADIENT_RED;
            case HEALER -> GUIUtil.GRADIENT_GREEN;
        };
    }
}
