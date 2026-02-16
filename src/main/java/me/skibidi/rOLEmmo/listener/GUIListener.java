package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.gui.RoleInfoGUI;
import me.skibidi.rolemmo.gui.TitleGUI;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.manager.TitleManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Listener để xử lý các GUI interactions
 */
public class GUIListener implements Listener {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;
    private final TitleManager titleManager;
    
    // Track page cho Title GUI (auto-cleanup khi player quit)
    private final Map<UUID, Integer> titlePages = new HashMap<>();

    public GUIListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
        this.titleManager = plugin.getTitleManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.isOnline()) return; // Player đã offline

        String title = event.getView().getTitle();
        if (title == null) return;
        
        ItemStack clicked = event.getCurrentItem();

        // Cancel tất cả clicks trong GUI - check với cả formatted titles
        String strippedTitle = org.bukkit.ChatColor.stripColor(title);
        boolean isRoleInfoGUI = strippedTitle.contains("ROLE INFO") || title.contains("§6Role Info");
        boolean isTitleGUI = strippedTitle.contains("DANH HIỆU") || title.contains("§6Danh Hiệu");
        boolean isSkillsGUI = strippedTitle.contains("SKILLS") || (title.contains("§6Skills") && !title.contains("Chọn"));
        boolean isUpgradeGUI = strippedTitle.contains("UPGRADE") || title.startsWith("§6Upgrade:");
        boolean isSkillDetailGUI = strippedTitle.contains("CHI TIET SKILL");
        boolean isRoleSelectGUI = strippedTitle.contains("CHỌN ROLE") || title.contains("§6Chọn Role");
        boolean isRoleChangeGUI = strippedTitle.contains("ĐỔI ROLE") || title.contains("§6Đổi Role");
        
        if (isRoleInfoGUI || isTitleGUI || isSkillsGUI || isUpgradeGUI || 
            isSkillDetailGUI || isRoleSelectGUI || isRoleChangeGUI) {
            event.setCancelled(true);

            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Role Info GUI
            if (isRoleInfoGUI) {
                handleRoleInfoClick(player, event.getSlot(), clicked);
            }
            // Title GUI
            else if (isTitleGUI) {
                handleTitleClick(player, event.getSlot(), clicked, title);
            }
            // Skill List GUI
            else if (isSkillsGUI) {
                handleSkillListClick(player, event.getSlot(), clicked, title);
            }
            // Skill Upgrade GUI
            else if (isUpgradeGUI) {
                handleSkillUpgradeClick(player, event.getSlot(), clicked, title);
            }
            // Skill Detail GUI (chi tiết 1 skill + Upgrade + Sử dụng)
            else if (isSkillDetailGUI) {
                handleSkillDetailClick(player, event.getSlot(), clicked);
            }
            // Role Select GUI
            else if (isRoleSelectGUI) {
                handleRoleSelectClick(player, event.getSlot(), clicked);
            }
            // Role Change GUI
            else if (isRoleChangeGUI) {
                handleRoleChangeClick(player, event.getSlot(), clicked);
            }
        }
    }

    /**
     * Xử lý click trong Role Info GUI
     */
    private void handleRoleInfoClick(Player player, int slot, ItemStack clicked) {
        // Close button (BARRIER hoặc Paper icon từ resource pack)
        if (slot == 49 && (clicked.getType() == Material.BARRIER || clicked.getType() == Material.PAPER)) {
            player.closeInventory();
            return;
        }

        // Skills button (một nút: xem danh sách + chọn + upgrade)
        if (slot == 29 && (clicked.getType() == Material.ENCHANTED_BOOK || clicked.getType() == Material.PAPER)) {
            me.skibidi.rolemmo.gui.SkillListGUI.open(player, plugin);
            return;
        }

        // Titles button (NAME_TAG hoặc Paper icon)
        if (slot == 31 && (clicked.getType() == Material.NAME_TAG || clicked.getType() == Material.PAPER)) {
            TitleGUI.open(player, plugin);
            return;
        }

        // Change role button (ENDER_PEARL hoặc Paper icon)
        if (slot == 33 && (clicked.getType() == Material.ENDER_PEARL || clicked.getType() == Material.PAPER)) {
            me.skibidi.rolemmo.gui.RoleChangeGUI.open(player, plugin);
            return;
        }
    }

    /**
     * Xử lý click trong Title GUI
     */
    private void handleTitleClick(Player player, int slot, ItemStack clicked, String inventoryTitle) {
        // Back to role info button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            RoleInfoGUI.open(player, plugin);
            return;
        }

        // Remove active title / Close button (BARRIER ở slot 49)
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            Title activeTitle = titleManager.getActiveTitle(player);
            if (activeTitle != null) {
                // Có active title -> remove
                titleManager.setActiveTitle(player, null);
                // Refresh GUI
                TitleGUI.open(player, plugin);
            } else {
                // Không có active title -> close
                player.closeInventory();
            }
            return;
        }

        // Close button (red glass)
        if (slot == 48 && clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
            player.closeInventory();
            return;
        }

        // Navigation buttons
        if (clicked.getType() == Material.ARROW) {
            List<Title> allTitles = titleManager.getUnlockedTitles(player);
            Title activeTitle = titleManager.getActiveTitle(player);
            int currentPage = titlePages.getOrDefault(player.getUniqueId(), 0);

            if (slot == 45) { // Back
                if (currentPage > 0) {
                    titlePages.put(player.getUniqueId(), currentPage - 1);
                    TitleGUI.open(player, plugin, allTitles, activeTitle, currentPage - 1);
                }
            } else if (slot == 53) { // Next
                int totalPages = (int) Math.ceil((double) allTitles.size() / 28.0);
                if (currentPage < totalPages - 1) {
                    titlePages.put(player.getUniqueId(), currentPage + 1);
                    TitleGUI.open(player, plugin, allTitles, activeTitle, currentPage + 1);
                }
            }
            return;
        }

        // Title items (check if clicked item is a title)
        int[] titleSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int titleSlot : titleSlots) {
            if (slot == titleSlot) {
                // Tìm title tương ứng với item được click
                List<Title> titles = titleManager.getUnlockedTitles(player);
                int page = titlePages.getOrDefault(player.getUniqueId(), 0);
                int startIndex = page * 28;
                int index = slot - titleSlots[0];
                
                if (index >= 0 && index < 28 && (startIndex + index) < titles.size()) {
                    Title clickedTitle = titles.get(startIndex + index);
                    if (clickedTitle != null) {
                        // Set active title
                        titleManager.setActiveTitle(player, clickedTitle);
                        // Refresh GUI
                        TitleGUI.open(player, plugin);
                    }
                }
                return;
            }
        }
    }

    /**
     * Xử lý click trong Skill List GUI
     */
    private void handleSkillListClick(Player player, int slot, ItemStack clicked, String inventoryTitle) {
        // Back button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            RoleInfoGUI.open(player, plugin);
            return;
        }

        // Close button (BARRIER hoặc Paper icon)
        if (slot == 49 && (clicked.getType() == Material.BARRIER || clicked.getType() == Material.PAPER)) {
            player.closeInventory();
            return;
        }

        // Skill items
        int[] skillSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        for (int skillSlot : skillSlots) {
            if (slot == skillSlot) {
                // Tìm skill tương ứng
                Role currentRole = roleManager.getPlayerRole(player);
                if (currentRole == null) {
                    return;
                }

                var skills = plugin.getSkillManager().getSkills(currentRole);
                int index = -1;
                for (int i = 0; i < skillSlots.length; i++) {
                    if (skillSlots[i] == slot) {
                        index = i;
                        break;
                    }
                }

                if (index >= 0 && index < skills.size()) {
                    var skill = skills.get(index);
                    // Mở Skill Detail GUI (chi tiết + Upgrade + Sử dụng skill)
                    me.skibidi.rolemmo.gui.SkillDetailGUI.open(player, plugin, skill);
                }
                return;
            }
        }
    }

    /**
     * Xử lý click trong Skill Detail GUI (chi tiết 1 skill)
     */
    private void handleSkillDetailClick(Player player, int slot, ItemStack clicked) {
        // Back button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            me.skibidi.rolemmo.gui.SkillDetailGUI.clearDetail(player);
            me.skibidi.rolemmo.gui.SkillListGUI.open(player, plugin);
            return;
        }

        // Close button (BARRIER hoặc Paper icon)
        if (slot == 49 && (clicked.getType() == Material.BARRIER || clicked.getType() == Material.PAPER)) {
            me.skibidi.rolemmo.gui.SkillDetailGUI.clearDetail(player);
            player.closeInventory();
            return;
        }

        // Nút Upgrade (slot 30)
        if (slot == 30 && (clicked.getType() == Material.EMERALD || clicked.getType() == Material.REDSTONE)) {
            me.skibidi.rolemmo.model.Skill skill = me.skibidi.rolemmo.gui.SkillDetailGUI.getSkillForDetail(player);
            if (skill != null) {
                int level = plugin.getSkillManager().getPlayerSkillLevel(player, skill.getId());
                if (level < skill.getMaxLevel()) {
                    int required = plugin.getConfigManager().getSkillUpgradeCost(level);
                    if (plugin.getRoleManager().getSkillPoints(player) >= required) {
                        try {
                            plugin.getSkillManager().upgradeSkill(player, skill.getId());
                            me.skibidi.rolemmo.gui.SkillDetailGUI.open(player, plugin, skill);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error upgrading skill: " + e.getMessage());
                            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cLỗi khi nâng cấp skill!");
                        }
                    }
                }
            }
            return;
        }

        // Nút Sử dụng skill này (slot 32, Paper icon)
        if (slot == 32 && (clicked.getType() == Material.PAPER || clicked.getType() == Material.BLAZE_ROD)) {
            me.skibidi.rolemmo.model.Skill skill = me.skibidi.rolemmo.gui.SkillDetailGUI.getSkillForDetail(player);
            if (skill != null && plugin.getSkillManager().selectSkill(player, skill.getId())) {
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§aĐã chọn skill: " + skill.getName());
                me.skibidi.rolemmo.gui.SkillDetailGUI.clearDetail(player);
                player.closeInventory();
            }
            return;
        }
    }

    /**
     * Xử lý click trong Skill Upgrade GUI
     */
    private void handleSkillUpgradeClick(Player player, int slot, ItemStack clicked, String inventoryTitle) {
        // Back button (về danh sách skill hoặc Role Info)
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            me.skibidi.rolemmo.gui.SkillDetailGUI.clearDetail(player);
            me.skibidi.rolemmo.gui.SkillListGUI.open(player, plugin);
            return;
        }

        // Close button
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Upgrade button
        if (slot == 31 && (clicked.getType() == Material.EMERALD || clicked.getType() == Material.REDSTONE)) {
            // Extract skill ID từ title với player context
            String skillId = extractSkillIdFromTitle(inventoryTitle, player);
            if (skillId != null) {
                var skillManager = plugin.getSkillManager();
                if (skillManager != null) {
                    try {
                        skillManager.upgradeSkill(player, skillId);
                        // Refresh GUI
                        var skill = skillManager.getSkill(skillId);
                        if (skill != null) {
                            me.skibidi.rolemmo.gui.SkillUpgradeGUI.open(player, plugin, skill);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error upgrading skill: " + e.getMessage());
                        me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cLỗi khi nâng cấp skill! Vui lòng thử lại.");
                    }
                }
            } else {
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cKhông tìm thấy skill! Vui lòng thử lại.");
            }
            return;
        }
    }

    /**
     * Extract skill ID từ inventory title
     */
    private String extractSkillIdFromTitle(String title) {
        if (title == null) return null;
        
        // Title format có thể là: "§6Upgrade: SkillName" hoặc formatted title
        String strippedTitle = org.bukkit.ChatColor.stripColor(title);
        String skillName = null;
        
        if (title.startsWith("§6Upgrade: ")) {
            skillName = title.substring(12); // Remove "§6Upgrade: "
        } else if (strippedTitle.contains("UPGRADE:")) {
            // Extract từ formatted title: "⚡ UPGRADE: SkillName"
            int index = strippedTitle.indexOf("UPGRADE:");
            if (index >= 0) {
                skillName = strippedTitle.substring(index + 8).trim();
            }
        }
        
        if (skillName == null || skillName.isEmpty()) return null;
        
        // Tìm skill theo name
        var skillManager = plugin.getSkillManager();
        if (skillManager == null) return null;
        
        Player player = null;
        try {
            // Try to get player from event context - fallback to null if not available
            // This method should receive player as parameter
            return null; // Will be fixed in handleSkillUpgradeClick
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract skill ID từ inventory title với player context
     */
    private String extractSkillIdFromTitle(String title, Player player) {
        if (title == null || player == null) return null;
        
        // Title format có thể là: "§6Upgrade: SkillName" hoặc formatted title
        String strippedTitle = org.bukkit.ChatColor.stripColor(title);
        String skillName = null;
        
        if (title.startsWith("§6Upgrade: ")) {
            skillName = title.substring(12); // Remove "§6Upgrade: "
        } else if (strippedTitle.contains("UPGRADE:")) {
            // Extract từ formatted title: "⚡ UPGRADE: SkillName"
            int index = strippedTitle.indexOf("UPGRADE:");
            if (index >= 0) {
                skillName = strippedTitle.substring(index + 8).trim();
            }
        }
        
        if (skillName == null || skillName.isEmpty()) return null;
        
        // Tìm skill theo name
        var skillManager = plugin.getSkillManager();
        if (skillManager == null) return null;
        
        Role currentRole = roleManager.getPlayerRole(player);
        if (currentRole == null) return null;
        
        for (var skill : skillManager.getSkills(currentRole)) {
            if (skill != null && skill.getName().equals(skillName)) {
                return skill.getId();
            }
        }
        
        return null;
    }

    /**
     * Xử lý click trong Role Select GUI
     */
    private void handleRoleSelectClick(Player player, int slot, ItemStack clicked) {
        // Slot dùng Paper + CustomModelData (icon pack); vẫn chấp nhận material cũ
        boolean tanker = (clicked.getType() == Material.SHIELD || clicked.getType() == Material.PAPER) && slot == 20;
        boolean dps    = (clicked.getType() == Material.DIAMOND_SWORD || clicked.getType() == Material.PAPER) && slot == 22;
        boolean healer = (clicked.getType() == Material.GOLDEN_APPLE || clicked.getType() == Material.PAPER) && slot == 24;

        if (tanker) {
            if (roleManager.selectRole(player, me.skibidi.rolemmo.model.Role.TANKER)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
            return;
        }
        if (dps) {
            if (roleManager.selectRole(player, me.skibidi.rolemmo.model.Role.DPS)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
            return;
        }
        if (healer) {
            if (roleManager.selectRole(player, me.skibidi.rolemmo.model.Role.HEALER)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
        }
    }

    /**
     * Xử lý click trong Role Change GUI
     */
    private void handleRoleChangeClick(Player player, int slot, ItemStack clicked) {
        // Back button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            RoleInfoGUI.open(player, plugin);
            return;
        }

        // Close button (BARRIER hoặc Paper icon)
        if (slot == 49 && (clicked.getType() == Material.BARRIER || clicked.getType() == Material.PAPER)) {
            player.closeInventory();
            return;
        }

        // TANKER / DPS / HEALER (SHIELD, DIAMOND_SWORD, GOLDEN_APPLE hoặc Paper icon)
        boolean tanker = (clicked.getType() == Material.SHIELD || clicked.getType() == Material.PAPER) && slot == 20;
        boolean dps    = (clicked.getType() == Material.DIAMOND_SWORD || clicked.getType() == Material.PAPER) && slot == 22;
        boolean healer = (clicked.getType() == Material.GOLDEN_APPLE || clicked.getType() == Material.PAPER) && slot == 24;

        if (tanker) {
            me.skibidi.rolemmo.model.Role targetRole = me.skibidi.rolemmo.model.Role.TANKER;
            if (roleManager.getPlayerRole(player) != targetRole && roleManager.changeRole(player, targetRole)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
            return;
        }
        if (dps) {
            me.skibidi.rolemmo.model.Role targetRole = me.skibidi.rolemmo.model.Role.DPS;
            if (roleManager.getPlayerRole(player) != targetRole && roleManager.changeRole(player, targetRole)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
            return;
        }
        if (healer) {
            me.skibidi.rolemmo.model.Role targetRole = me.skibidi.rolemmo.model.Role.HEALER;
            if (roleManager.getPlayerRole(player) != targetRole && roleManager.changeRole(player, targetRole)) {
                player.closeInventory();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> RoleInfoGUI.open(player, plugin), 5L);
            }
        }
    }

    /**
     * Clean up titlePages khi player quit để tránh memory leak
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != null) {
            titlePages.remove(event.getPlayer().getUniqueId());
        }
    }
}
