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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    
    // Track page cho Title GUI
    private final Map<UUID, Integer> titlePages = new HashMap<>();

    public GUIListener(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
        this.titleManager = plugin.getTitleManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        // Cancel tất cả clicks trong GUI
        if (title.startsWith("§6Role Info") || 
            title.startsWith("§6Danh Hiệu") ||
            title.startsWith("§6Skills") ||
            title.startsWith("§6Upgrade:") ||
            title.startsWith("§6Chọn Skill")) {
            event.setCancelled(true);

            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Role Info GUI
            if (title.startsWith("§6Role Info")) {
                handleRoleInfoClick(player, event.getSlot(), clicked);
            }
            // Title GUI
            else if (title.startsWith("§6Danh Hiệu")) {
                handleTitleClick(player, event.getSlot(), clicked, title);
            }
            // Skill List GUI
            else if (title.startsWith("§6Skills") && !title.contains("Chọn")) {
                handleSkillListClick(player, event.getSlot(), clicked, title);
            }
            // Skill Upgrade GUI
            else if (title.startsWith("§6Upgrade:")) {
                handleSkillUpgradeClick(player, event.getSlot(), clicked, title);
            }
            // Skill Selection GUI
            else if (title.startsWith("§6Chọn Skill")) {
                handleSkillSelectionClick(player, event.getSlot(), clicked, title);
            }
        }
    }

    /**
     * Xử lý click trong Role Info GUI
     */
    private void handleRoleInfoClick(Player player, int slot, ItemStack clicked) {
        // Close button
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Skills button
        if (slot == 29 && clicked.getType() == Material.ENCHANTED_BOOK) {
            me.skibidi.rolemmo.gui.SkillListGUI.open(player, plugin);
            return;
        }

        // Chọn Skill button
        if (slot == 30 && clicked.getType() == Material.BLAZE_ROD) {
            me.skibidi.rolemmo.gui.SkillSelectionGUI.open(player, plugin);
            return;
        }

        // Titles button
        if (slot == 31 && clicked.getType() == Material.NAME_TAG) {
            TitleGUI.open(player, plugin);
            return;
        }

        // Change role button
        if (slot == 33 && clicked.getType() == Material.ENDER_PEARL) {
            // TODO: Mở Change Role GUI (sẽ implement sau)
            player.sendMessage("§eSử dụng /role change <role> để đổi role!");
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

        // Close button
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
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
                    // Mở Skill Upgrade GUI
                    me.skibidi.rolemmo.gui.SkillUpgradeGUI.open(player, plugin, skill);
                }
                return;
            }
        }
    }

    /**
     * Xử lý click trong Skill Upgrade GUI
     */
    private void handleSkillUpgradeClick(Player player, int slot, ItemStack clicked, String inventoryTitle) {
        // Back button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
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
            // Extract skill ID từ title
            String skillId = extractSkillIdFromTitle(inventoryTitle);
            if (skillId != null) {
                var skillManager = plugin.getSkillManager();
                if (skillManager != null) {
                    skillManager.upgradeSkill(player, skillId);
                    // Refresh GUI
                    var skill = skillManager.getSkill(skillId);
                    if (skill != null) {
                        me.skibidi.rolemmo.gui.SkillUpgradeGUI.open(player, plugin, skill);
                    }
                }
            }
            return;
        }
    }

    /**
     * Extract skill ID từ inventory title
     */
    private String extractSkillIdFromTitle(String title) {
        // Title format: "§6Upgrade: SkillName"
        if (title.startsWith("§6Upgrade: ")) {
            String skillName = title.substring(12); // Remove "§6Upgrade: "
            // Tìm skill theo name
            var skillManager = plugin.getSkillManager();
            if (skillManager != null) {
                Role currentRole = roleManager.getPlayerRole(plugin.getServer().getPlayer(player.getName()));
                if (currentRole != null) {
                    for (var skill : skillManager.getSkills(currentRole)) {
                        if (skill.getName().equals(skillName)) {
                            return skill.getId();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Xử lý click trong Skill Selection GUI
     */
    private void handleSkillSelectionClick(Player player, int slot, ItemStack clicked, String inventoryTitle) {
        // Back button
        if (slot == 48 && clicked.getType() == Material.ARROW) {
            RoleInfoGUI.open(player, plugin);
            return;
        }

        // Close button
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
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
                    // Chọn skill
                    if (plugin.getSkillManager().selectSkill(player, skill.getId())) {
                        // Refresh GUI
                        me.skibidi.rolemmo.gui.SkillSelectionGUI.open(player, plugin);
                    }
                }
                return;
            }
        }
    }
}
