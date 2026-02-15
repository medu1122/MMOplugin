package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.LevelManager;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.manager.TitleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Title;
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

        // Role info ở center với font lớn và màu sắc đẹp
        ItemStack roleItem = new ItemStack(getMaterialForRole(currentRole));
        ItemMeta roleMeta = roleItem.getItemMeta();
        if (roleMeta != null) {
            String roleIcon = GUIUtil.getRoleIcon(currentRole);
            String[] roleGradient = getGradientForRole(currentRole);
            roleMeta.setDisplayName(GUIUtil.createLargeTitle(roleIcon + " " + currentRole.getDisplayName(), roleGradient));
            
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            
            int level = roleManager.getRoleLevel(player, currentRole);
            int exp = roleManager.getRoleExp(player, currentRole);
            int requiredExp = levelManager.getRequiredExpForNextLevel(player, currentRole);
            int skillPoints = roleManager.getSkillPoints(player);
            
            // Level với gradient
            lore.add(GUIUtil.COLOR_INFO + "§l📊 LEVEL: " + GUIUtil.gradientText(String.valueOf(level), GUIUtil.GRADIENT_GOLD) + 
                    GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText("999", GUIUtil.GRADIENT_GOLD));
            
            // Exp với progress bar
            lore.add(GUIUtil.COLOR_SECONDARY + "§l⭐ EXP: " + GUIUtil.gradientText(String.valueOf(exp), GUIUtil.GRADIENT_BLUE) + 
                    GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText(String.valueOf(requiredExp), GUIUtil.GRADIENT_BLUE));
            
            if (level < 999) {
                double progress = (double) exp / requiredExp;
                String progressBar = GUIUtil.createProgressBar(progress, 20, GUIUtil.COLOR_SUCCESS, GUIUtil.COLOR_MUTED);
                lore.add(" " + progressBar + " §e" + String.format("%.1f", progress * 100) + "%");
            }
            
            lore.add(" ");
            lore.add(GUIUtil.COLOR_HIGHLIGHT + "§l✨ SKILL POINTS: " + GUIUtil.gradientText(String.valueOf(skillPoints), GUIUtil.GRADIENT_PURPLE));
            
            // Active title
            Title activeTitle = titleManager.getActiveTitle(player);
            if (activeTitle != null) {
                lore.add(" ");
                lore.add(GUIUtil.COLOR_PRIMARY + "§l🏆 DANH HIỆU: " + GUIUtil.gradientText(activeTitle.getDisplayName(), GUIUtil.GRADIENT_GOLD));
            }
            
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            roleMeta.setLore(lore);
            roleItem.setItemMeta(roleMeta);
        }
        inv.setItem(22, roleItem);

        // Skills button với icon và màu sắc đẹp
        ItemStack skillsButton = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta skillsMeta = skillsButton.getItemMeta();
        if (skillsMeta != null) {
            skillsMeta.setDisplayName(GUIUtil.createLargeTitle("📚 XEM SKILLS", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§lClick để xem tất cả skills");
            lore.add(GUIUtil.COLOR_MUTED + "của role " + GUIUtil.gradientText(currentRole.getDisplayName(), getGradientForRole(currentRole)));
            int skillCount = plugin.getSkillManager().getSkills(currentRole).size();
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lSố skills: " + GUIUtil.gradientText(String.valueOf(skillCount), GUIUtil.GRADIENT_BLUE));
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            skillsMeta.setLore(lore);
            skillsButton.setItemMeta(skillsMeta);
        }
        inv.setItem(29, skillsButton);

        // Chọn Skill button với icon và màu sắc đẹp
        ItemStack selectSkillButton = new ItemStack(Material.BLAZE_ROD);
        ItemMeta selectSkillMeta = selectSkillButton.getItemMeta();
        if (selectSkillMeta != null) {
            selectSkillMeta.setDisplayName(GUIUtil.createLargeTitle("⚡ CHỌN SKILL", GUIUtil.GRADIENT_PURPLE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            String selectedSkillId = plugin.getSkillManager().getSelectedSkillId(player);
            if (selectedSkillId != null) {
                var selectedSkill = plugin.getSkillManager().getSkill(selectedSkillId);
                if (selectedSkill != null) {
                    lore.add(GUIUtil.COLOR_SUCCESS + "§lSkill đang dùng:");
                    lore.add(GUIUtil.gradientText("  " + selectedSkill.getName(), GUIUtil.GRADIENT_GOLD));
                }
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§lSkill đang dùng:");
                lore.add(GUIUtil.COLOR_MUTED + "  Chưa chọn");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lClick để chọn skill!");
            lore.add(GUIUtil.COLOR_MUTED + "Cooldown: " + GUIUtil.COLOR_WARNING + "30 phút");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            selectSkillMeta.setLore(lore);
            selectSkillButton.setItemMeta(selectSkillMeta);
        }
        inv.setItem(30, selectSkillButton);

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

        // Change role button với icon và màu sắc đẹp
        ItemStack changeRoleButton = new ItemStack(Material.ENDER_PEARL);
        ItemMeta changeMeta = changeRoleButton.getItemMeta();
        if (changeMeta != null) {
            changeMeta.setDisplayName(GUIUtil.createLargeTitle("🔄 ĐỔI ROLE", GUIUtil.GRADIENT_PURPLE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            if (roleManager.canChangeRoleForFree(player)) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§lCó thể đổi miễn phí!");
                lore.add(" ");
                lore.add(GUIUtil.COLOR_SECONDARY + "§lClick để đổi role");
            } else if (roleManager.canChangeRole(player)) {
                long cost = plugin.getConfigManager().getRoleChangeCost();
                lore.add(GUIUtil.COLOR_WARNING + "§lCost: " + GUIUtil.gradientText(cost + " coins", GUIUtil.GRADIENT_GOLD));
                lore.add(GUIUtil.COLOR_MUTED + "Hoặc đợi: " + GUIUtil.COLOR_INFO + roleManager.getTimeUntilCanChange(player));
                lore.add(" ");
                lore.add(GUIUtil.COLOR_SECONDARY + "§lClick để đổi role");
            } else {
                lore.add(GUIUtil.COLOR_ERROR + "§lChưa thể đổi role!");
                lore.add(GUIUtil.COLOR_MUTED + "Cần đợi: " + GUIUtil.COLOR_INFO + roleManager.getTimeUntilCanChange(player));
                lore.add(" ");
                lore.add(GUIUtil.COLOR_MUTED + "Click để xem thông tin");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            changeMeta.setLore(lore);
            changeRoleButton.setItemMeta(changeMeta);
        }
        inv.setItem(33, changeRoleButton);

        // Close button với icon
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(GUIUtil.COLOR_ERROR + "§l✖ ĐÓNG");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        // Glass panes decoration với màu sắc đa dạng
        ItemStack glass = GUIUtil.createGlassPane("gray");

        // Fill empty slots
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                if (i != 22 && i != 29 && i != 30 && i != 31 && i != 33 && i != 49) {
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
