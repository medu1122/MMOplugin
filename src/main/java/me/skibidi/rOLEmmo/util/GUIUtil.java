package me.skibidi.rolemmo.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để tạo GUI items với font lớn và màu sắc đẹp
 */
public class GUIUtil {

    // Unicode characters để tạo font lớn
    private static final String BOLD_START = "§l";
    private static final String RESET = "§r";
    
    // Color codes đa dạng
    public static final String COLOR_PRIMARY = "§6"; // Gold
    public static final String COLOR_SECONDARY = "§e"; // Yellow
    public static final String COLOR_SUCCESS = "§a"; // Green
    public static final String COLOR_ERROR = "§c"; // Red
    public static final String COLOR_INFO = "§b"; // Aqua
    public static final String COLOR_WARNING = "§e"; // Yellow
    public static final String COLOR_HIGHLIGHT = "§d"; // Light Purple
    public static final String COLOR_MUTED = "§7"; // Gray
    
    // Gradient colors
    public static final String[] GRADIENT_GOLD = {"§6", "§e", "§f"};
    public static final String[] GRADIENT_RED = {"§c", "§4", "§c"};
    public static final String[] GRADIENT_GREEN = {"§a", "§2", "§a"};
    public static final String[] GRADIENT_BLUE = {"§b", "§3", "§b"};
    public static final String[] GRADIENT_PURPLE = {"§d", "§5", "§d"};

    /**
     * Tạo title lớn với gradient
     */
    public static String createLargeTitle(String text, String[] gradient) {
        if (gradient == null || gradient.length == 0) {
            return BOLD_START + COLOR_PRIMARY + text + RESET;
        }
        
        StringBuilder result = new StringBuilder(BOLD_START);
        int gradientIndex = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(" ");
            } else {
                result.append(gradient[gradientIndex % gradient.length]).append(c);
                gradientIndex++;
            }
        }
        return result.append(RESET).toString();
    }

    /**
     * Tạo text với màu gradient
     */
    public static String gradientText(String text, String[] gradient) {
        if (gradient == null || gradient.length == 0) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        int gradientIndex = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(" ");
            } else {
                result.append(gradient[gradientIndex % gradient.length]).append(c);
                gradientIndex++;
            }
        }
        return result.toString();
    }

    /**
     * Tạo item với display name lớn và đẹp
     */
    public static ItemStack createStyledItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Tạo glass pane với màu
     */
    public static ItemStack createGlassPane(String color) {
        Material glassType = switch (color.toLowerCase()) {
            case "gray" -> Material.GRAY_STAINED_GLASS_PANE;
            case "blue" -> Material.BLUE_STAINED_GLASS_PANE;
            case "green" -> Material.GREEN_STAINED_GLASS_PANE;
            case "red" -> Material.RED_STAINED_GLASS_PANE;
            case "yellow" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "purple" -> Material.PURPLE_STAINED_GLASS_PANE;
            case "orange" -> Material.ORANGE_STAINED_GLASS_PANE;
            default -> Material.GRAY_STAINED_GLASS_PANE;
        };
        
        ItemStack glass = new ItemStack(glassType);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        return glass;
    }

    /**
     * Format số với màu gradient
     */
    public static String formatNumber(int number, String[] gradient) {
        return gradientText(String.valueOf(number), gradient);
    }

    /**
     * Tạo progress bar với màu sắc
     */
    public static String createProgressBar(double progress, int length, String filledColor, String emptyColor) {
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;
        
        int filled = (int) (progress * length);
        int empty = length - filled;
        
        StringBuilder bar = new StringBuilder();
        bar.append(filledColor);
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append(emptyColor);
        for (int i = 0; i < empty; i++) {
            bar.append("█");
        }
        return bar.toString();
    }

    /**
     * Tạo separator line
     */
    public static String createSeparator(String color) {
        return color + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }

    /**
     * Tạo lore với formatting đẹp
     */
    public static List<String> createStyledLore(String... lines) {
        List<String> lore = new ArrayList<>();
        for (String line : lines) {
            if (line.isEmpty()) {
                lore.add(" ");
            } else {
                lore.add(line);
            }
        }
        return lore;
    }

    /**
     * Tạo icon emoji cho role
     */
    public static String getRoleIcon(me.skibidi.rolemmo.model.Role role) {
        return switch (role) {
            case TANKER -> "🛡️";
            case DPS -> "⚔️";
            case HEALER -> "✝️";
        };
    }

    /**
     * Tạo icon emoji cho skill
     */
    public static String getSkillIcon(String skillId) {
        return switch (skillId.toLowerCase()) {
            case "fireball" -> "🔥";
            default -> "✨";
        };
    }
}
