package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.SkillManager;
import me.skibidi.rolemmo.model.Role;
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

/**
 * GUI để hiển thị danh sách skills của role
 */
public class SkillListGUI {

    /**
     * Mở GUI skill list cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        var roleManager = plugin.getRoleManager();
        var skillManager = plugin.getSkillManager();

        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) {
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cBạn chưa chọn role!");
            return;
        }

        skillManager.ensureDefaultSkillLevels(player, currentRole);

        List<Skill> skills = skillManager.getSkills(currentRole);
        if (skills.isEmpty()) {
            openEmpty(player, plugin);
            return;
        }

        String[] roleGradient = getGradientForRole(currentRole);
        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("📚 SKILLS", GUIUtil.GRADIENT_BLUE) + 
                " " + GUIUtil.gradientText(currentRole.getFullDisplayName(), roleGradient));

        // Info item ở slot 4 với font lớn
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(GUIUtil.createLargeTitle("📖 THÔNG TIN", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§lRole: " + GUIUtil.gradientText(currentRole.getFullDisplayName(), roleGradient));
            lore.add(GUIUtil.COLOR_SECONDARY + "§lTổng số skills: " + GUIUtil.gradientText(String.valueOf(skills.size()), GUIUtil.GRADIENT_GOLD));
            lore.add(GUIUtil.COLOR_HIGHLIGHT + "§lSkill Points: " + GUIUtil.gradientText(String.valueOf(roleManager.getSkillPoints(player)), GUIUtil.GRADIENT_PURPLE));
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click vào skill để xem chi tiết!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
            int currentLevel = skillManager.getPlayerSkillLevel(player, skill.getId());
            
            ItemStack skillItem = createSkillItem(skill, currentLevel, skillManager, roleManager, player, plugin);
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

        // Close button – icon từ pack
        inv.setItem(49, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CLOSE, "§cDong", null));

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
        Inventory inv = Bukkit.createInventory(null, 54, "§6Skills");

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

        inv.setItem(49, RolemmoIcons.createIcon(RolemmoIcons.ICON_BTN_CLOSE, "§cDong", null));

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
     * Tạo ItemStack cho skill với font lớn và màu sắc đẹp
     */
    private static ItemStack createSkillItem(Skill skill, int currentLevel, SkillManager skillManager, 
                                            me.skibidi.rolemmo.manager.RoleManager roleManager, Player player, ROLEmmo plugin) {
        String skillIcon = GUIUtil.getSkillIcon(skill.getId());
        String displayName = GUIUtil.createLargeTitle(skillIcon + " " + skill.getName(), GUIUtil.GRADIENT_PURPLE);
            
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§l" + skill.getDescription());
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lLevel hiện tại: " + 
                    GUIUtil.gradientText(String.valueOf(currentLevel), GUIUtil.GRADIENT_BLUE) + 
                    GUIUtil.COLOR_MUTED + " / " + GUIUtil.gradientText(String.valueOf(skill.getMaxLevel()), GUIUtil.GRADIENT_BLUE));
            
            if (currentLevel < skill.getMaxLevel()) {
                int requiredPoints = plugin.getConfigManager().getSkillUpgradeCost(currentLevel);
                lore.add(GUIUtil.COLOR_WARNING + "§lLevel tiếp theo cần: " + 
                        GUIUtil.gradientText(requiredPoints + " điểm", GUIUtil.GRADIENT_GOLD));
            } else {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Đã đạt level tối đa!");
            }
            
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§lClick de xem chi tiet, upgrade va su dung!");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Dùng icon từ pack cho Fireball, còn lại dùng material
        if ("fireball".equalsIgnoreCase(skill.getId())) {
            return RolemmoIcons.createIcon(RolemmoIcons.ICON_SKILL_FIREBALL, displayName, lore);
        }
        ItemStack item = new ItemStack(getMaterialForSkill(skill));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
