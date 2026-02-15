package me.skibidi.rolemmo.gui;

import me.skibidi.rolemmo.ROLEmmo;
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
 * GUI để player chọn và sử dụng danh hiệu
 */
public class TitleGUI {

    private static final int TITLES_PER_PAGE = 28; // 7 rows x 4 columns
    private static final int[] TITLE_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    /**
     * Mở GUI danh hiệu cho player
     */
    public static void open(Player player, ROLEmmo plugin) {
        TitleManager titleManager = plugin.getTitleManager();
        List<Title> allTitles = titleManager.getUnlockedTitles(player);
        Title activeTitle = titleManager.getActiveTitle(player);

        // Nếu chưa có title nào, hiển thị thông báo
        if (allTitles.isEmpty()) {
            openEmpty(player, plugin);
            return;
        }

        open(player, plugin, allTitles, activeTitle, 0);
    }

    /**
     * Mở GUI với page cụ thể
     */
    public static void open(Player player, ROLEmmo plugin, List<Title> titles, Title activeTitle, int page) {
        TitleManager titleManager = plugin.getTitleManager();
        int totalPages = (int) Math.ceil((double) titles.size() / TITLES_PER_PAGE);

        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, GUIUtil.createLargeTitle("🏆 DANH HIỆU", GUIUtil.GRADIENT_GOLD) + 
                GUIUtil.COLOR_MUTED + " §7(Page " + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Info item ở slot 4 với font lớn
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(GUIUtil.createLargeTitle("📖 THÔNG TIN", GUIUtil.GRADIENT_BLUE));
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SECONDARY + "§lTổng số danh hiệu: " + GUIUtil.gradientText(String.valueOf(titles.size()), GUIUtil.GRADIENT_GOLD));
            if (activeTitle != null) {
                lore.add(" ");
                lore.add(GUIUtil.COLOR_PRIMARY + "§lDanh hiệu đang dùng:");
                lore.add(GUIUtil.gradientText("  " + activeTitle.getDisplayName(), GUIUtil.GRADIENT_GOLD));
            } else {
                lore.add(" ");
                lore.add(GUIUtil.COLOR_ERROR + "§lDanh hiệu đang dùng:");
                lore.add(GUIUtil.COLOR_MUTED + "  Không có");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Click vào danh hiệu để sử dụng!");
            lore.add(GUIUtil.COLOR_MUTED + "Danh hiệu đã unlock sẽ có thể sử dụng.");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Hiển thị titles
        int startIndex = page * TITLES_PER_PAGE;
        int endIndex = Math.min(startIndex + TITLES_PER_PAGE, titles.size());

        for (int i = startIndex; i < endIndex; i++) {
            Title title = titles.get(i);
            int slot = TITLE_SLOTS[i - startIndex];

            ItemStack titleItem = createTitleItem(title, activeTitle);
            inv.setItem(slot, titleItem);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Trang trước");
                back.setItemMeta(backMeta);
            }
            inv.setItem(45, back);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Trang sau");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(53, next);
        }

        // Remove active title button (chỉ hiện nếu có active title)
        if (activeTitle != null) {
            ItemStack removeActive = new ItemStack(Material.BARRIER);
            ItemMeta removeMeta = removeActive.getItemMeta();
            if (removeMeta != null) {
                removeMeta.setDisplayName("§cGỡ Danh Hiệu");
                List<String> lore = new ArrayList<>();
                lore.add("§7Click để gỡ danh hiệu đang dùng");
                removeMeta.setLore(lore);
                removeActive.setItemMeta(removeMeta);
            }
            inv.setItem(49, removeActive);
        } else {
            // Close button nếu không có active title
            ItemStack close = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = close.getItemMeta();
            if (closeMeta != null) {
                closeMeta.setDisplayName("§cĐóng");
                close.setItemMeta(closeMeta);
            }
            inv.setItem(49, close);
        }

        // Back to role info button
        ItemStack backToRole = new ItemStack(Material.ARROW);
        ItemMeta backToRoleMeta = backToRole.getItemMeta();
        if (backToRoleMeta != null) {
            backToRoleMeta.setDisplayName("§7Quay lại Role Info");
            backToRole.setItemMeta(backToRoleMeta);
        }
        inv.setItem(48, backToRole);

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
                // Don't fill title slots
                boolean isTitleSlot = false;
                for (int slot : TITLE_SLOTS) {
                    if (i == slot) {
                        isTitleSlot = true;
                        break;
                    }
                }
                if (!isTitleSlot && i != 4 && i != 45 && i != 48 && i != 49 && i != 53) {
                    inv.setItem(i, glass);
                }
            }
        }

        player.openInventory(inv);
    }

    /**
     * Mở GUI khi chưa có title nào
     */
    private static void openEmpty(Player player, ROLEmmo plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Danh Hiệu");

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Chưa Có Danh Hiệu");
            List<String> lore = new ArrayList<>();
            lore.add("§7Bạn chưa unlock danh hiệu nào!");
            lore.add("");
            lore.add("§eCách unlock danh hiệu:");
            lore.add("§7- Level up role của bạn");
            lore.add("§7- Mỗi level sẽ unlock danh hiệu tương ứng");
            lore.add("");
            lore.add("§7Danh hiệu sẽ được giữ mãi mãi");
            lore.add("§7ngay cả khi bạn đổi role!");
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
     * Tạo ItemStack cho title với font lớn và màu sắc đẹp
     */
    private static ItemStack createTitleItem(Title title, Title activeTitle) {
        Material material = getMaterialForRole(title.getRole());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            boolean isActive = activeTitle != null && activeTitle.getId().equals(title.getId());
            
            // Display name với font lớn
            String displayName;
            if (isActive) {
                displayName = GUIUtil.createLargeTitle("✓ " + title.getDisplayName(), GUIUtil.GRADIENT_GREEN) + 
                        GUIUtil.COLOR_SUCCESS + " §l(ĐANG DÙNG)";
            } else {
                displayName = GUIUtil.createLargeTitle(title.getDisplayName(), GUIUtil.GRADIENT_GOLD);
            }
            meta.setDisplayName(displayName);

            // Lore với formatting đẹp
            List<String> lore = new ArrayList<>();
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            lore.add(" ");
            lore.add(GUIUtil.COLOR_INFO + "§lRole: " + GUIUtil.gradientText(title.getRole().getFullDisplayName(), 
                    getGradientForRole(title.getRole())));
            lore.add(GUIUtil.COLOR_SECONDARY + "§lLevel yêu cầu: " + GUIUtil.gradientText(String.valueOf(title.getRequiredLevel()), GUIUtil.GRADIENT_BLUE));
            lore.add(" ");
            
            if (isActive) {
                lore.add(GUIUtil.COLOR_SUCCESS + "§l✓ Đang sử dụng danh hiệu này!");
            } else {
                lore.add(GUIUtil.COLOR_WARNING + "§lClick để sử dụng danh hiệu này!");
            }
            lore.add(" ");
            lore.add(GUIUtil.COLOR_MUTED + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
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
     * Lấy Material phù hợp cho role
     */
    private static Material getMaterialForRole(Role role) {
        return switch (role) {
            case TANKER -> Material.SHIELD;
            case DPS -> Material.DIAMOND_SWORD;
            case HEALER -> Material.GOLDEN_APPLE;
        };
    }
}
