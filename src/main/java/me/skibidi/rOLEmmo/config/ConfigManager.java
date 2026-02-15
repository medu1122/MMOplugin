package me.skibidi.rolemmo.config;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final ROLEmmo plugin;
    private FileConfiguration config;

    public ConfigManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    // ========== ROLE CHANGE CONFIG ==========

    public long getRoleChangeCooldown() {
        return config.getLong("role_change.cooldown_hours", 24) * 60 * 60 * 1000; // Convert to milliseconds
    }

    public long getRoleChangeCost() {
        return config.getLong("role_change.cost_coins", 10);
    }

    // ========== TITLE CONFIG ==========

    /**
     * Lấy danh sách danh hiệu theo level cho một role
     */
    public Map<Integer, String> getTitlesForRole(Role role) {
        Map<Integer, String> titles = new LinkedHashMap<>();
        ConfigurationSection roleSection = config.getConfigurationSection("titles." + role.name().toLowerCase());
        if (roleSection != null) {
            for (String levelStr : roleSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelStr);
                    String title = roleSection.getString(levelStr);
                    if (title != null) {
                        titles.put(level, title);
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid level in titles config: " + levelStr);
                }
            }
        }
        return titles;
    }

    /**
     * Lấy danh hiệu ở level cụ thể cho role
     */
    public String getTitleAtLevel(Role role, int level) {
        return config.getString("titles." + role.name().toLowerCase() + "." + level);
    }

    // ========== SKILL CONFIG ==========

    /**
     * Lấy cost để upgrade skill từ level hiện tại lên level tiếp theo
     */
    public int getSkillUpgradeCost(int currentLevel) {
        return config.getInt("skills.upgrade_costs." + currentLevel, 1);
    }

    /**
     * Lấy tất cả skill upgrade costs
     */
    public Map<Integer, Integer> getAllSkillUpgradeCosts() {
        Map<Integer, Integer> costs = new HashMap<>();
        ConfigurationSection costsSection = config.getConfigurationSection("skills.upgrade_costs");
        if (costsSection != null) {
            for (String levelStr : costsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelStr);
                    int cost = costsSection.getInt(levelStr);
                    costs.put(level, cost);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid level in skill upgrade costs: " + levelStr);
                }
            }
        }
        return costs;
    }

    // ========== EXPERIENCE CONFIG ==========

    /**
     * Lấy tỷ lệ convert exp nhân vật thành exp role
     */
    public double getExpConversionRate() {
        return config.getDouble("experience.conversion_rate", 1.0);
    }

    /**
     * Lấy exp cần để lên level tiếp theo
     * Có thể config công thức hoặc giá trị cố định
     */
    public int getRequiredExpForLevel(int currentLevel) {
        // Default formula: level * 100
        int baseExp = config.getInt("experience.base_exp_per_level", 100);
        return currentLevel * baseExp;
    }

    // ========== MESSAGES ==========

    public String getMessage(String path) {
        return config.getString("messages." + path, "§cMessage not found: " + path);
    }

    public String getMessage(String path, String defaultValue) {
        return config.getString("messages." + path, defaultValue);
    }

    // ========== LUCKPERMS CONFIG ==========

    /**
     * Lấy đường dẫn đến folder LuckPerms (để copy config)
     * Nếu không config thì trả về null
     */
    public String getLuckPermsFolderPath() {
        return config.getString("luckperms.folder_path", null);
    }

    /**
     * Lấy group name trong LuckPerms cho role
     */
    public String getLuckPermsGroup(Role role) {
        return config.getString("luckperms.groups." + role.name().toLowerCase(), role.name().toLowerCase());
    }

    // ========== DATABASE CONFIG ==========

    public String getDatabasePath() {
        return config.getString("database.path", "rolemmo.db");
    }
}
