package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI để chọn skill cho role hiện tại
 * Cooldown 30 phút giữa các lần đổi skill
 */
public class SkillSelectionGUI {

    private static final long SKILL_CHANGE_COOLDOWN = 30 * 60 * 1000L; // 30 phút

    /**
     * Mở GUI chọn skill cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        var roleManager = plugin.getRoleManager();
        var skillManager = plugin.getSkillManager();

        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) {
            player.sendMessage("§cBạn chưa chọn role!");
            return;
        }

        List<Skill> skills = skillManager.getSkills(currentRole);
        if (skills.isEmpty()) {
            openEmpty(player, plugin);
            return;
        }

        String selectedSkillId = skillManager.getSelectedSkillId(player);
        long lastSkillChange = skillManager.getLastSkillChange(player);
        long timeSinceChange = System.currentTimeMillis() - lastSkillChange;
        boolean canChange = timeSinceChange >= SKILL_CHANGE_COOLDOWN;
        long remainingMinutes = (SKILL_CHANGE_COOLDOWN - timeSinceChange) / (60 * 1000);

        Inventory inv = Bukkit.createInventory(null, 54, "§6Chọn Skill - " + currentRole.getFullDisplayName());

        // Info item ở slot 4
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Thông Tin Chọn Skill");
            List<String> lore = new ArrayList<>();
            lore.add("§7Role: " + currentRole.getFullDisplayName());
            lore.add("§7Số skills: §e" + skills.size());
            lore.add("");
            if (selectedSkillId != null) {
                Skill selectedSkill = skillManager.getSkill(selectedSkillId);
                if (selectedSkill != null) {
                    lore.add("§7Skill đang dùng: §e" + selectedSkill.getName());
                }
            } else {
                lore.add("§7Skill đang dùng: §cChưa chọn");
            }
            lore.add("");
            if (canChange) {
                lore.add("§aCó thể đổi skill!");
            } else {
                lore.add("§cCooldown: §e" + remainingMinutes + " phút");
                lore.add("§7Còn lại: §e" + formatTime(remainingMinutes * 60 * 1000));
            }
            lore.add("");
            lore.add("§eClick vào skill để chọn!");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Hiển thị skills
        int[] skillSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < skills.size() && i < skillSlots.length; i++) {
            Skill skill = skills.get(i);
            int slot = skillSlots[i];
            boolean isSelected = skill.getId().equals(selectedSkillId);
            int skillLevel = skillManager.getPlayerSkillLevel(player, skill.getId());
            
            ItemStack skillItem = createSkillItem(skill, isSelected, skillLevel, canChange, plugin);
            inv.setItem(slot, skillItem);
        }

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
                boolean isSkillSlot = false;
                for (int skillSlot : skillSlots) {
                    if (i == skillSlot) {
                        isSkillSlot = true;
                        break;
                    }
                }
                if (!isSkillSlot && i != 4 && i != 48 && i != 49) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Mở GUI khi chưa có skill nào
     */
    private static void openEmpty(Player player, ROLEmmo plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Chọn Skill");

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Chưa Có Skills");
            List<String> lore = new ArrayList<>();
            lore.add("§7Role của bạn chưa có skills!");
            lore.add("§7Skills sẽ được thêm sau.");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(22, infoItem);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        // Glass panes
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null && i != 22 && i != 49) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Tạo ItemStack cho skill
     */
    private static ItemStack createSkillItem(Skill skill, boolean isSelected, int skillLevel, 
                                             boolean canChange, ROLEmmo plugin) {
        Material material = getMaterialForSkill(skill);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName = "§e" + skill.getName();
            if (isSelected) {
                displayName = "§a§l✓ " + skill.getName() + " §a§l(ĐANG DÙNG)";
            }
            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            lore.add("§7" + skill.getDescription());
            lore.add("");
            lore.add("§7Level: §e" + skillLevel + "§7/§e" + skill.getMaxLevel());
            
            if (skillLevel < 1) {
                lore.add("§cChưa học skill này!");
            }
            
            lore.add("");
            if (isSelected) {
                lore.add("§a§lĐang sử dụng skill này!");
            } else if (canChange) {
                lore.add("§eClick để chọn skill này!");
            } else {
                lore.add("§cKhông thể đổi skill!");
                lore.add("§7Cần đợi 30 phút từ lần đổi cuối");
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

    /**
     * Format time (milliseconds) thành string
     */
    private static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
