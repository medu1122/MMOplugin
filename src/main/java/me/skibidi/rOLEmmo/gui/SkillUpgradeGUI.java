package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Skill;
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

        Inventory inv = Bukkit.createInventory(null, 54, "§6Upgrade: " + skill.getName());

        // Skill info ở center
        ItemStack skillItem = new ItemStack(getMaterialForSkill(skill));
        ItemMeta skillMeta = skillItem.getItemMeta();
        if (skillMeta != null) {
            skillMeta.setDisplayName("§e" + skill.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + skill.getDescription());
            lore.add("");
            lore.add("§7Level hiện tại: §e" + currentLevel + "§7/§e" + skill.getMaxLevel());
            lore.add("§7Skill Points: §e" + skillPoints);
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

        // Upgrade button (nếu có thể)
        if (currentLevel < skill.getMaxLevel()) {
            int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
            boolean canUpgrade = skillPoints >= requiredPoints;

            ItemStack upgradeButton = new ItemStack(canUpgrade ? Material.EMERALD : Material.REDSTONE);
            ItemMeta upgradeMeta = upgradeButton.getItemMeta();
            if (upgradeMeta != null) {
                upgradeMeta.setDisplayName(canUpgrade ? "§aUpgrade Skill" : "§cKhông đủ điểm");
                List<String> lore = new ArrayList<>();
                lore.add("§7Từ level §e" + currentLevel + " §7lên §e" + (currentLevel + 1));
                lore.add("§7Cần: §e" + requiredPoints + " điểm");
                lore.add("§7Bạn có: §e" + skillPoints + " điểm");
                lore.add("");
                if (canUpgrade) {
                    lore.add("§eClick để upgrade!");
                } else {
                    lore.add("§cKhông đủ skill points!");
                }
                upgradeMeta.setLore(lore);
                upgradeButton.setItemMeta(upgradeMeta);
            }
            inv.setItem(31, upgradeButton);
        } else {
            // Max level reached
            ItemStack maxLevelItem = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta maxMeta = maxLevelItem.getItemMeta();
            if (maxMeta != null) {
                maxMeta.setDisplayName("§a§lĐã đạt level tối đa!");
                List<String> lore = new ArrayList<>();
                lore.add("§7Skill đã được nâng cấp tối đa!");
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
     * Tạo ItemStack cho level info
     */
    private static ItemStack createLevelItem(int level, Skill.SkillLevelInfo levelInfo, int currentLevel, 
                                             int skillPoints, ROLEmmo plugin) {
        Material material = level <= currentLevel ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName = level <= currentLevel ? "§aLevel " + level : "§7Level " + level;
            if (level == currentLevel) {
                displayName = "§e§lLevel " + level + " §e§l(HIỆN TẠI)";
            }
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Damage: §c" + levelInfo.getDamage() + " HP");
            lore.add("§7Cooldown: §e" + levelInfo.getCooldown() + "s");
            
            // Fireball count
            int fireballCount = levelInfo.getPropertyInt("fireballCount", 5);
            lore.add("§7Số cầu lửa: §e" + fireballCount);
            
            // Burn effect
            double burnPercent = levelInfo.getPropertyDouble("burnDurationPercent", 0.0);
            if (burnPercent > 0) {
                lore.add("§7Hiệu ứng đốt: §c+" + String.format("%.0f", burnPercent) + "%");
            }
            
            lore.add("");
            if (level <= currentLevel) {
                lore.add("§aĐã học");
            } else if (level == currentLevel + 1) {
                int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
                lore.add("§7Cần: §e" + requiredPoints + " điểm");
                if (skillPoints >= requiredPoints) {
                    lore.add("§aCó thể upgrade!");
                } else {
                    lore.add("§cKhông đủ điểm!");
                }
            } else {
                lore.add("§7Chưa thể học");
            }
            
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
