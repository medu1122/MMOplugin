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
            title.startsWith("§6Skills")) {
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
            // TODO: Mở Skill List GUI (sẽ implement sau)
            player.sendMessage("§eTính năng Skills sẽ được thêm sau!");
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
}
