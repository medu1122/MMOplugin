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

        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("🔄 ĐỔI ROLE", GUIUtil.GRADIENT_PURPLE));

        // Info item ở center với font lớn
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(GUIUtil.createLargeTitle("📖 THÔNG TIN", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            String[] roleGradient = getGradientForRole(currentRole);
            lore.add(GUIUtil.COLOR_INFO + "§lRole hiện tại: " + GUIUtil.gradientText(currentRole.getFullDisplayName(), roleGradient));
            lore.add(" ");
            if (canChangeForFree) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Có thể đổi role miễn phí!");
            } else if (canChange) {
                lore.add(GUIUtil.COLOR_WARNING + "§lCost để đổi ngay: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
                lore.add(GUIUtil.COLOR_MUTED + "Hoặc đợi: " + GUIUtil.COLOR_INFO + timeUntilCanChange);
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Chưa thể đổi role!");
                lore.add(GUIUtil.COLOR_MUTED + "Cần đợi: " + GUIUtil.COLOR_INFO + timeUntilCanChange);
                lore.add(GUIUtil.COLOR_MUTED + "Hoặc trả: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Chọn role bên dưới để đổi!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(22, infoItem);

        // TANKER role với font lớn
        ItemStack tankerItem = new ItemStack(Material.SHIELD);
        ItemMeta tankerMeta = tankerItem.getItemMeta();
        if (tankerMeta != null) {
            boolean isCurrent = currentRole == Role.TANKER;
            String displayName = isCurrent ? 
                    GUIUtil.createLargeTitle("🛡️ TANKER", GUIUtil.GRADIENT_BLUE) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" :
                    GUIUtil.createLargeTitle("🛡️ TANKER", GUIUtil.GRADIENT_BLUE);
            tankerMeta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§lHệ Hộ Thể / Kim Cang");
            lore.add(" ");
            if (isCurrent) {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Đây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để đổi sang role này!");
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Chưa thể đổi role!");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            tankerMeta.setLore(lore);
            tankerItem.setItemMeta(tankerMeta);
        }
        inv.setItem(20, tankerItem);

        // DPS role với font lớn
        ItemStack dpsItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta dpsMeta = dpsItem.getItemMeta();
        if (dpsMeta != null) {
            boolean isCurrent = currentRole == Role.DPS;
            String displayName = isCurrent ? 
                    GUIUtil.createLargeTitle("⚔️ DPS", GUIUtil.GRADIENT_RED) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" :
                    GUIUtil.createLargeTitle("⚔️ DPS", GUIUtil.GRADIENT_RED);
            dpsMeta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_ERROR + "§lHệ Sát Phạt / Chiến Đạo");
            lore.add(" ");
            if (isCurrent) {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Đây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để đổi sang role này!");
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Chưa thể đổi role!");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            dpsMeta.setLore(lore);
            dpsItem.setItemMeta(dpsMeta);
        }
        inv.setItem(22, dpsItem); // Override info item

        // HEALER role với font lớn
        ItemStack healerItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healerMeta = healerItem.getItemMeta();
        if (healerMeta != null) {
            boolean isCurrent = currentRole == Role.HEALER;
            String displayName = isCurrent ? 
                    GUIUtil.createLargeTitle("✝️ HEALER", GUIUtil.GRADIENT_GREEN) + GUIUtil.COLOR_MUTED + " §l(Hiện tại)" :
                    GUIUtil.createLargeTitle("✝️ HEALER", GUIUtil.GRADIENT_GREEN);
            healerMeta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§lHệ Linh Lực / Thánh Đạo");
            lore.add(" ");
            if (isCurrent) {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Đây là role hiện tại của bạn!");
            } else if (canChange || canChangeForFree) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để đổi sang role này!");
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§l✖ Chưa thể đổi role!");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
