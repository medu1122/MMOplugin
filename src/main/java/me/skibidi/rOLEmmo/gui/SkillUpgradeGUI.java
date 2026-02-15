package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Skill;
import me.skibidi.rolemmo.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI để upgrade skill và xem chi tiết từng level
 */
public class SkillUpgradeGUI {

    /**
     * Mở GUI upgrade skill cho player
     */
    public static void open(Player player, ROLEmmo plugin, Skill skill) {
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại!");
            return;
        }

        SkillManager skillManager = plugin.getSkillManager();
        int currentLevel = skillManager.getPlayerSkillLevel(player, skill.getId());
        int skillPoints = plugin.getRoleManager().getSkillPoints(player);

        String skillIcon = GUIUtil.getSkillIcon(skill.getId());
        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("⚡ UPGRADE: " + skill.getName(), GUIUtil.GRADIENT_PURPLE));

        // Skill info ở center với font lớn
        ItemStack skillItem = new ItemStack(getMaterialForSkill(skill));
        ItemMeta skillMeta = skillItem.getItemMeta();
        if (skillMeta != null) {
            skillMeta.setDisplayName(GUIUtil.createLargeTitle(skillIcon + " " + skill.getName(), GUIUtil.GRADIENT_PURPLE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§l" + skill.getDescription());
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lLevel hiện tại: " + 
                    GUIUtil.gradientText(String.valueOf(currentLevel), GUIUtil.GRADIENT_BLUE) + 
                    GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText(String.valueOf(skill.getMaxLevel()), GUIUtil.GRADIENT_BLUE));
            lore.add(GUIUtil.COLOR_HIGHLIGHT + "§lSkill Points: " + GUIUtil.gradientText(String.valueOf(skillPoints), GUIUtil.GRADIENT_PURPLE));
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            skillMeta.setLore(lore);
            skillItem.setItemMeta(skillMeta);
        }
        inv.setItem(22, skillItem);

        // Hiển thị thông tin từng level
        int[] levelSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 23, 24, 25
        };

        for (int level = 1; level <= skill.getMaxLevel() && (level - 1) < levelSlots.length; level++) {
            int slot = levelSlots[level - 1];
            Skill.SkillLevelInfo levelInfo = skill.getLevelInfo(level);
            ItemStack levelItem = createLevelItem(level, levelInfo, currentLevel, skillPoints, plugin);
            inv.setItem(slot, levelItem);
        }

        // Upgrade button (nếu có thể) với font lớn
        if (currentLevel < skill.getMaxLevel()) {
            int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
            boolean canUpgrade = skillPoints >= requiredPoints;

            ItemStack upgradeButton = new ItemStack(canUpgrade ? Material.EMERALD : Material.REDSTONE);
            ItemMeta upgradeMeta = upgradeButton.getItemMeta();
            if (upgradeMeta != null) {
                upgradeMeta.setDisplayName(canUpgrade ? 
                        GUIUtil.createLargeTitle("✓ UPGRADE SKILL", GUIUtil.GRADIENT_GREEN) :
                        GUIUtil.createLargeTitle("✖ KHÔNG ĐỦ ĐIỂM", GUIUtil.GRADIENT_RED));
                List<String> lore = new ArrayList<>();
                lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                lore.add(" ");
                lore.add(GUIUtil.COLOR_SECONDARY + "§lTừ level " + GUIUtil.gradientText(String.valueOf(currentLevel), GUIUtil.GRADIENT_BLUE) + 
                        GUIUtil.COLOR_MUTED + " lên " + GUIUtil.gradientText(String.valueOf(currentLevel + 1), GUIUtil.GRADIENT_BLUE));
                lore.add(GUIUtil.COLOR_WARNING + "§lCần: " + GUIUtil.gradientText(requiredPoints + " điểm", GUIUtil.GRADIENT_GOLD));
                lore.add(GUIUtil.COLOR_HIGHLIGHT + "§lBạn có: " + GUIUtil.gradientText(skillPoints + " điểm", GUIUtil.GRADIENT_PURPLE));
                lore.add(" ");
                if (canUpgrade) {
                    lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click để upgrade!");
                } else {
                    lore.add(GUIUtil.COLOR_ERROR + "§l✖ Không đủ skill points!");
                }
                lore.add(" ");
                lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                upgradeMeta.setLore(lore);
                upgradeButton.setItemMeta(upgradeMeta);
            }
            inv.setItem(31, upgradeButton);
        } else {
            // Max level reached với font lớn
            ItemStack maxLevelItem = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta maxMeta = maxLevelItem.getItemMeta();
            if (maxMeta != null) {
                maxMeta.setDisplayName(GUIUtil.createLargeTitle("✓ ĐÃ ĐẠT LEVEL TỐI ĐA", GUIUtil.GRADIENT_GOLD));
                List<String> lore = new ArrayList<>();
                lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                lore.add(" ");
                lore.add(GUIUtil.COLOR_SUCCESS + "§lSkill đã được nâng cấp tối đa!");
                lore.add(" ");
                lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                maxMeta.setLore(lore);
                maxLevelItem.setItemMeta(maxMeta);
            }
            inv.setItem(31, maxLevelItem);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Quay lại Skill List");
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
                boolean isLevelSlot = false;
                for (int levelSlot : levelSlots) {
                    if (i == levelSlot) {
                        isLevelSlot = true;
                        break;
                    }
                }
                if (!isLevelSlot && i != 22 && i != 31 && i != 48 && i != 49) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Tạo ItemStack cho level info với font lớn và màu sắc đẹp
     */
    private static ItemStack createLevelItem(int level, Skill.SkillLevelInfo levelInfo, int currentLevel, 
                                             int skillPoints, ROLEmmo plugin) {
        Material material = level <= currentLevel ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName;
            if (level == currentLevel) {
                displayName = GUIUtil.createLargeTitle("LEVEL " + level, GUIUtil.GRADIENT_GOLD) + 
                        GUIUtil.COLOR_WARNING + " §l(HIỆN TẠI)";
            } else if (level <= currentLevel) {
                displayName = GUIUtil.createLargeTitle("LEVEL " + level, GUIUtil.GRADIENT_GREEN);
            } else {
                displayName = GUIUtil.COLOR_MUTED + "§lLevel " + level;
            }
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_ERROR + "§lDamage: " + GUIUtil.gradientText(levelInfo.getDamage() + " HP", GUIUtil.GRADIENT_RED));
            lore.add(GUIUtil.COLOR_WARNING + "§lCooldown: " + GUIUtil.gradientText(levelInfo.getCooldown() + "s", GUIUtil.GRADIENT_GOLD));
            
            // Fireball count
            int fireballCount = levelInfo.getPropertyInt("fireballCount", 5);
            lore.add(GUIUtil.COLOR_INFO + "§lSố cầu lửa: " + GUIUtil.gradientText(String.valueOf(fireballCount), GUIUtil.GRADIENT_BLUE));
            
            // Burn effect
            double burnPercent = levelInfo.getPropertyDouble("burnDurationPercent", 0.0);
            if (burnPercent > 0) {
                lore.add(GUIUtil.COLOR_ERROR + "§lHiệu ứng đốt: " + GUIUtil.gradientText("+" + String.format("%.0f", burnPercent) + "%", GUIUtil.GRADIENT_RED));
            }
            
            lore.add(" ");
            if (level <= currentLevel) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Đã học");
            } else if (level == currentLevel + 1) {
                int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
                lore.add(GUIUtil.COLOR_WARNING + "§lCần: " + GUIUtil.gradientText(requiredPoints + " điểm", GUIUtil.GRADIENT_GOLD));
                if (skillPoints >= requiredPoints) {
                    lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Có thể upgrade!");
                } else {
                    lore.add(GUIUtil.COLOR_ERROR + "§l✖ Không đủ điểm!");
                }
            } else {
                lore.add(GUIUtil.COLOR_MUTED + "Chưa thể học");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Lấy Material phù hợp cho skill
     */
    private static Material getMaterialForSkill(Skill skill) {
        return switch (skill.getRole()) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.BLAZE_ROD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }
}
