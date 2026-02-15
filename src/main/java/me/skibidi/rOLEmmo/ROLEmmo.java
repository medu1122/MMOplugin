package me.skibidi.rolemmo;

import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ROLEmmo extends JavaPlugin {

    private static ROLEmmo instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

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
        
        getLogger().info("ROLEmmo plugin has been enabled!");
    }

    @Override
    public void onDisable() {
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
}
