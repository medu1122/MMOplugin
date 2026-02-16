package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Skill;
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
import java.util.Map;
import java.util.UUID;

/**
 * GUI chi tiết một skill: thông tin đầy đủ + nút Upgrade + nút Sử dụng skill này.
 * Mở từ SkillListGUI khi click vào một skill.
 */
public class SkillDetailGUI {

    /** Prefix trong title để listener nhận diện */
    public static final String TITLE_PREFIX = "CHI TIET SKILL: ";

    private static final Map<UUID, Skill> detailCache = new java.util.HashMap<>();

    /**
     * Mở GUI chi tiết skill cho player
     */
    public static void open(Player player, ROLEmmo plugin, Skill skill) {
        if (player == null || skill == null) return;
        detailCache.put(player.getUniqueId(), skill);

        SkillManager skillManager = plugin.getSkillManager();
        int currentLevel = skillManager.getPlayerSkillLevel(player, skill.getId());
        int skillPoints = plugin.getRoleManager().getSkillPoints(player);
        boolean isSelected = skill.getId().equals(skillManager.getSelectedSkillId(player));

        String skillIcon = GUIUtil.getSkillIcon(skill.getId());
        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("CHI TIET SKILL", GUIUtil.GRADIENT_PURPLE) + " " + skill.getName());

        // Slot 22: thông tin chi tiết skill
        ItemStack skillItem = new ItemStack(getMaterialForSkill(skill));
        ItemMeta skillMeta = skillItem.getItemMeta();
        if (skillMeta != null) {
            skillMeta.setDisplayName(GUIUtil.createLargeTitle(skillIcon + " " + skill.getName(), GUIUtil.GRADIENT_PURPLE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§l" + skill.getDescription());
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lLevel: " + GUIUtil.gradientText(String.valueOf(currentLevel), GUIUtil.GRADIENT_BLUE) +
                    GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText(String.valueOf(skill.getMaxLevel()), GUIUtil.GRADIENT_BLUE));
            lore.add(GUIUtil.COLOR_HIGHLIGHT + "§lSkill Points: " + GUIUtil.gradientText(String.valueOf(skillPoints), GUIUtil.GRADIENT_PURPLE));
            if (currentLevel >= 1 && currentLevel <= skill.getMaxLevel()) {
                Skill.SkillLevelInfo levelInfo = skill.getLevelInfo(currentLevel);
                lore.add(" ");
                lore.add(GUIUtil.COLOR_ERROR + "§lDamage: " + levelInfo.getDamage() + " HP");
                lore.add(GUIUtil.COLOR_WARNING + "§lCooldown: " + levelInfo.getCooldown() + "s");
            }
            lore.add(" ");
            if (isSelected) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§lDang su dung skill nay!");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            skillMeta.setLore(lore);
            skillItem.setItemMeta(skillMeta);
        }
        inv.setItem(22, skillItem);

        // Slot 30: nút Upgrade – luôn hiển thị tên "NÂNG CẤP SKILL", lore giải thích đủ/không đủ điểm
        if (currentLevel < skill.getMaxLevel()) {
            int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
            boolean canUpgrade = skillPoints >= requiredPoints;
            ItemStack upgradeBtn = new ItemStack(canUpgrade ? Material.EMERALD : Material.REDSTONE);
            ItemMeta upgradeMeta = upgradeBtn.getItemMeta();
            if (upgradeMeta != null) {
                upgradeMeta.setDisplayName(GUIUtil.createLargeTitle("NANG CAP SKILL", canUpgrade ? GUIUtil.GRADIENT_GREEN : GUIUtil.GRADIENT_RED));
                List<String> upgradeLore = new ArrayList<>();
                upgradeLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                upgradeLore.add(" ");
                upgradeLore.add(GUIUtil.COLOR_SECONDARY + "§lLen level " + (currentLevel + 1) + " – Can: " + requiredPoints + " diem");
                upgradeLore.add(GUIUtil.COLOR_HIGHLIGHT + "§lBan co: " + skillPoints + " diem");
                upgradeLore.add(" ");
                if (canUpgrade) {
                    upgradeLore.add(GUIUtil.COLOR_SUCCESS + "§lClick de nang cap!");
                } else {
                    upgradeLore.add(GUIUtil.COLOR_ERROR + "§lKhong du diem! Can them " + (requiredPoints - skillPoints) + " diem.");
                }
                upgradeLore.add(" ");
                upgradeLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                upgradeMeta.setLore(upgradeLore);
                upgradeBtn.setItemMeta(upgradeMeta);
            }
            inv.setItem(30, upgradeBtn);
        } else {
            List<String> maxLore = new ArrayList<>();
            maxLore.add(GUIUtil.COLOR_SUCCESS + "§lDa dat level toi da!");
            inv.setItem(30, new ItemStack(Material.GOLD_BLOCK));
            ItemMeta maxMeta = inv.getItem(30).getItemMeta();
            if (maxMeta != null) {
                maxMeta.setDisplayName(GUIUtil.createLargeTitle("DA DAT MAX", GUIUtil.GRADIENT_GOLD));
                maxMeta.setLore(maxLore);
                inv.getItem(30).setItemMeta(maxMeta);
            }
        }

        // Slot 32: nút Sử dụng skill này
        List<String> useLore = new ArrayList<>();
        useLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        useLore.add(" ");
        useLore.add(isSelected ? GUIUtil.COLOR_SUCCESS + "§lDang su dung skill nay!" : GUIUtil.COLOR_SECONDARY + "§lChon lam skill hoat dong");
        useLore.add(" ");
        useLore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        inv.setItem(32, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_SELECT_SKILL,
                isSelected ? GUIUtil.COLOR_SUCCESS + "§lDANG DUNG" : GUIUtil.createLargeTitle("SU DUNG SKILL NAY", GUIUtil.GRADIENT_GREEN), useLore));

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Quay lai danh sach Skills");
            back.setItemMeta(backMeta);
        }
        inv.setItem(48, back);

        // Close button
        inv.setItem(49, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CLOSE, "§cDong", null));

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null && i != 22 && i != 30 && i != 32 && i != 48 && i != 49) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    public static Skill getSkillForDetail(Player player) {
        return player == null ? null : detailCache.get(player.getUniqueId());
    }

    public static void clearDetail(Player player) {
        if (player != null) detailCache.remove(player.getUniqueId());
    }

    private static Material getMaterialForSkill(Skill skill) {
        return switch (skill.getRole()) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.BLAZE_ROD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }
}
