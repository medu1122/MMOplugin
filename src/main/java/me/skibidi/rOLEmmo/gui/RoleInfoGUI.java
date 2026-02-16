package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.LevelManager;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.manager.TitleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Title;
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
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cBạn chưa chọn role! Sử dụng /role select");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§6Role Info");

        // Role info ở center – dùng icon từ resource pack (Paper + CustomModelData)
        String[] roleGradient = getGradientForRole(currentRole);
        String roleDisplayName = GUIUtil.createBoldRoleName(currentRole);
        List<String> roleLore = new ArrayList<>();
        roleLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        roleLore.add(" ");
        int level = roleManager.getRoleLevel(player, currentRole);
        int exp = roleManager.getRoleExp(player, currentRole);
        int requiredExp = levelManager.getRequiredExpForNextLevel(player, currentRole);
        int skillPoints = roleManager.getSkillPoints(player);
        roleLore.add(GUIUtil.COLOR_INFO + "§l📊 LEVEL: " + GUIUtil.gradientText(String.valueOf(level), GUIUtil.GRADIENT_GOLD) + GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText("999", GUIUtil.GRADIENT_GOLD));
        roleLore.add(GUIUtil.COLOR_SECONDARY + "§l⭐ EXP: " + GUIUtil.gradientText(String.valueOf(exp), GUIUtil.GRADIENT_BLUE) + GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText(String.valueOf(requiredExp), GUIUtil.GRADIENT_BLUE));
        if (level < 999) {
            double progress = (double) exp / requiredExp;
            roleLore.add(" " + GUIUtil.createProgressBar(progress, 20, GUIUtil.COLOR_SUCCESS, GUIUtil.COLOR_MUTED) + " §e" + String.format("%.1f", progress * 100) + "%");
        }
        roleLore.add(" ");
        roleLore.add(GUIUtil.COLOR_HIGHLIGHT + "§l✨ SKILL POINTS: " + GUIUtil.gradientText(String.valueOf(skillPoints), GUIUtil.GRADIENT_PURPLE));
        Title activeTitle = titleManager.getActiveTitle(player);
        if (activeTitle != null) {
            roleLore.add(" ");
            roleLore.add(GUIUtil.COLOR_PRIMARY + "§l🏆 DANH HIỆU: " + GUIUtil.gradientText(activeTitle.getDisplayName(), GUIUtil.GRADIENT_GOLD));
        }
        roleLore.add(" ");
        roleLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(22, RolemmoIcons.createIcon(RolemmoIcons.getRoleIconId(currentRole), roleDisplayName, roleLore));

        // Skills button – xem danh sách + chọn + upgrade (một nút gộp)
        List<String> skillsLore = new ArrayList<>();
        skillsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        skillsLore.add(" ");
        skillsLore.add(GUIUtil.COLOR_INFO + "§lClick để xem danh sách skills");
        skillsLore.add(GUIUtil.COLOR_MUTED + "của role " + GUIUtil.createBoldRoleName(currentRole));
        String selectedSkillId = plugin.getSkillManager().getSelectedSkillId(player);
        if (selectedSkillId != null) {
            var selectedSkill = plugin.getSkillManager().getSkill(selectedSkillId);
            if (selectedSkill != null) {
                skillsLore.add(" ");
                skillsLore.add(GUIUtil.COLOR_SUCCESS + "§lSkill đang dùng: " + selectedSkill.getName());
            }
        }
        skillsLore.add(" ");
        skillsLore.add(GUIUtil.COLOR_SECONDARY + "§lTrong đó: chọn skill, xem chi tiết, upgrade");
        skillsLore.add(" ");
        skillsLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(29, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_SKILLS, GUIUtil.createLargeTitle("SKILLS", GUIUtil.GRADIENT_BLUE), skillsLore));

        // Titles button – icon từ pack
        List<String> titlesLore = new ArrayList<>();
        List<Title> unlockedTitles = titleManager.getUnlockedTitles(player);
        titlesLore.add("§7Click để xem danh hiệu");
        titlesLore.add("§7Đã unlock: §e" + unlockedTitles.size());
        if (titleManager.getActiveTitle(player) != null) {
            titlesLore.add("§7Đang dùng: " + titleManager.getActiveTitle(player).getDisplayName());
        }
        inv.setItem(31, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_TITLES, "§6Danh Hiệu", titlesLore));

        // Change role button – icon từ pack
        List<String> changeLore = new ArrayList<>();
        changeLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        changeLore.add(" ");
        if (roleManager.canChangeRoleForFree(player)) {
            changeLore.add(GUIUtil.COLOR_SUCCESS + "§lCó thể đổi miễn phí!");
            changeLore.add(" ");
            changeLore.add(GUIUtil.COLOR_SECONDARY + "§lClick để đổi role");
        } else if (roleManager.canChangeRole(player)) {
            long cost = plugin.getConfigManager().getRoleChangeCost();
            changeLore.add(GUIUtil.COLOR_WARNING + "§lCost: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
            changeLore.add(GUIUtil.COLOR_MUTED + "Hoặc đợi: " + GUIUtil.COLOR_INFO + roleManager.getTimeUntilCanChange(player));
            changeLore.add(" ");
            changeLore.add(GUIUtil.COLOR_SECONDARY + "§lClick để đổi role");
        } else {
            changeLore.add(GUIUtil.COLOR_ERROR + "§lChưa thể đổi role!");
            changeLore.add(GUIUtil.COLOR_MUTED + "Cần đợi: " + GUIUtil.COLOR_INFO + roleManager.getTimeUntilCanChange(player));
            changeLore.add(" ");
            changeLore.add(GUIUtil.COLOR_MUTED + "Click để xem thông tin");
        }
        changeLore.add(" ");
        changeLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(33, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CHANGE, GUIUtil.createLargeTitle("DOI ROLE", GUIUtil.GRADIENT_PURPLE), changeLore));

        // Close button – icon từ pack
        inv.setItem(49, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CLOSE, GUIUtil.COLOR_ERROR + "§lDONG", null));

        // Glass panes decoration với màu sắc đa dạng
        ItemStack glass = GUIUtil.createGlassPane("gray");

        // Fill empty slots (chỉ còn 1 nút Skills tại 29, bỏ slot 30)
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
