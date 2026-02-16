package me.skibidi.rolemmo.storage;

import me.skibidi.rolemmo.ROLEmmo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {

    private final ROLEmmo plugin;
    private Connection connection;
    private final String dbPath;

    public DatabaseManager(ROLEmmo plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dbPath = new File(dataFolder, "rolemmo.db").getAbsolutePath();
    }

    public synchronized void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        // Đóng connection cũ nếu có (tránh leak)
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore - đang cố reconnect
            }
        }

        try {
            // Driver bị shade/relocate thành me.skibidi.rolemmo.libs.sqlite (xem pom.xml)
            Class.forName("me.skibidi.rolemmo.libs.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Logger logger = plugin.getLogger();
            logger.info("Connected to SQLite database: " + dbPath);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found!", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        if (connection == null) {
            throw new SQLException("Failed to establish database connection");
        }
        return connection;
    }

    public void initTables() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            // Table: role_players - Lưu thông tin role, level, exp, skill points của player
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_players (
                    uuid TEXT PRIMARY KEY,
                    current_role TEXT,
                    tanker_level INTEGER DEFAULT 1,
                    tanker_exp INTEGER DEFAULT 0,
                    dps_level INTEGER DEFAULT 1,
                    dps_exp INTEGER DEFAULT 0,
                    healer_level INTEGER DEFAULT 1,
                    healer_exp INTEGER DEFAULT 0,
                    skill_points INTEGER DEFAULT 0,
                    last_role_change BIGINT DEFAULT 0,
                    selected_skill_id TEXT,
                    last_skill_change BIGINT DEFAULT 0
                )
            """);
            
            // Migration: Thêm columns nếu chưa có (cho database cũ)
            try {
                stmt.execute("ALTER TABLE role_players ADD COLUMN selected_skill_id TEXT");
            } catch (SQLException e) {
                // Column đã tồn tại, bỏ qua
            }
            try {
                stmt.execute("ALTER TABLE role_players ADD COLUMN last_skill_change BIGINT DEFAULT 0");
            } catch (SQLException e) {
                // Column đã tồn tại, bỏ qua
            }

            // Table: role_skills - Lưu skill level của từng player cho từng role
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_skills (
                    uuid TEXT,
                    skill_id TEXT,
                    level INTEGER DEFAULT 0,
                    PRIMARY KEY (uuid, skill_id)
                )
            """);

            // Table: role_titles - Lưu danh sách danh hiệu player đã sở hữu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_titles (
                    uuid TEXT,
                    title_id TEXT,
                    role TEXT,
                    unlocked_at BIGINT,
                    PRIMARY KEY (uuid, title_id)
                )
            """);

            // Table: role_active_title - Lưu danh hiệu đang active của player
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_active_title (
                    uuid TEXT PRIMARY KEY,
                    title_id TEXT
                )
            """);

            // Table: role_change_history - Lưu lịch sử đổi role (để check cooldown 1 ngày)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS role_change_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT,
                    from_role TEXT,
                    to_role TEXT,
                    changed_at BIGINT
                )
            """);

            plugin.getLogger().info("Database tables initialized successfully!");
        }
    }
}
