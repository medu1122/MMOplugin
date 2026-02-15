package me.skibidi.rolemmo;

import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ROLEmmo extends JavaPlugin {

    private static ROLEmmo instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private RoleManager roleManager;
    private me.skibidi.rolemmo.manager.TitleManager titleManager;
    private me.skibidi.rolemmo.manager.LevelManager levelManager;
    private me.skibidi.rolemmo.manager.SkillManager skillManager;
    private me.skibidi.rolemmo.listener.ActionbarListener actionbarListener; // Store để cancel khi disable

    @Override
    public void onEnable() {
        instance = this;
        
        // Load config first
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize database
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            databaseManager.initTables();
            getLogger().info("Database initialized successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        roleManager = new RoleManager(this);
        titleManager = new me.skibidi.rolemmo.manager.TitleManager(this);
        levelManager = new me.skibidi.rolemmo.manager.LevelManager(this, titleManager, roleManager);
        skillManager = new me.skibidi.rolemmo.manager.SkillManager(this, roleManager);
        
        // Initialize SkillItemUtil keys
        me.skibidi.rolemmo.util.SkillItemUtil.initializeKeys();
        
        // Register commands
        var roleCommand = getCommand("role");
        if (roleCommand != null) {
            roleCommand.setExecutor(new me.skibidi.rolemmo.command.RoleCommand(this));
            roleCommand.setTabCompleter(new me.skibidi.rolemmo.command.RoleTabCompleter());
        } else {
            getLogger().warning("Command 'role' not found in plugin.yml!");
        }
        
        var roleAdminCommand = getCommand("roleadmin");
        if (roleAdminCommand != null) {
            roleAdminCommand.setExecutor(new me.skibidi.rolemmo.command.RoleAdminCommand(this));
            roleAdminCommand.setTabCompleter(new me.skibidi.rolemmo.command.RoleAdminTabCompleter());
        } else {
            getLogger().warning("Command 'roleadmin' not found in plugin.yml!");
        }
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new me.skibidi.rolemmo.listener.ExperienceListener(this), this);
        getServer().getPluginManager().registerEvents(new me.skibidi.rolemmo.listener.PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new me.skibidi.rolemmo.listener.GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new me.skibidi.rolemmo.listener.SkillItemListener(this), this);
        getServer().getPluginManager().registerEvents(new me.skibidi.rolemmo.listener.DamageListener(this), this);
        
        // Start actionbar task
        actionbarListener = new me.skibidi.rolemmo.listener.ActionbarListener(this);
        
        // Log integrations status
        logIntegrationStatus();
        
        getLogger().info("ROLEmmo plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel actionbar task để tránh memory leak
        if (actionbarListener != null) {
            actionbarListener.cancelTask();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ROLEmmo plugin has been disabled!");
    }

    public static ROLEmmo getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public me.skibidi.rolemmo.manager.TitleManager getTitleManager() {
        return titleManager;
    }

    public me.skibidi.rolemmo.manager.LevelManager getLevelManager() {
        return levelManager;
    }

    public me.skibidi.rolemmo.manager.SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * Log status của các integrations
     */
    private void logIntegrationStatus() {
        if (roleManager == null) {
            getLogger().warning("RoleManager is null - cannot log integration status");
            return;
        }

        // Check LuckPerms
        try {
            if (roleManager.getLuckPermsManager() != null && roleManager.getLuckPermsManager().isEnabled()) {
                getLogger().info("✓ LuckPerms integration enabled");
            } else {
                getLogger().warning("✗ LuckPerms not found - rank system disabled");
            }
        } catch (Exception e) {
            getLogger().warning("Error checking LuckPerms: " + e.getMessage());
        }

        // Check MoneyPlugin
        try {
            if (roleManager.getMoneyPluginManager() != null && roleManager.getMoneyPluginManager().isEnabled()) {
                getLogger().info("✓ MoneyPlugin integration enabled");
            } else {
                getLogger().warning("✗ MoneyPlugin not found - role change cost disabled");
            }
        } catch (Exception e) {
            getLogger().warning("Error checking MoneyPlugin: " + e.getMessage());
        }

        // Check ClanCore
        try {
            if (roleManager.getClanCoreManager() != null && roleManager.getClanCoreManager().isEnabled()) {
                getLogger().info("✓ ClanCore integration enabled");
            } else {
                getLogger().warning("✗ ClanCore not found - team protection disabled");
            }
        } catch (Exception e) {
            getLogger().warning("Error checking ClanCore: " + e.getMessage());
        }
    }
}
