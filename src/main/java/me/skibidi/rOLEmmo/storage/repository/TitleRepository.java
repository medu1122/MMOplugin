package me.skibidi.rolemmo.storage.repository;

import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.storage.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TitleRepository {

    private final DatabaseManager databaseManager;

    public TitleRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Lấy tất cả danh hiệu đã unlock của player
     */
    public List<String> getUnlockedTitles(UUID uuid) throws SQLException {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT title_id FROM role_titles WHERE uuid = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString("title_id"));
                }
            }
        }
        return titles;
    }

    /**
     * Lấy danh hiệu đã unlock của player theo role
     */
    public List<String> getUnlockedTitlesByRole(UUID uuid, Role role) throws SQLException {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT title_id FROM role_titles WHERE uuid = ? AND role = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString("title_id"));
                }
            }
        }
        return titles;
    }

    /**
     * Unlock danh hiệu cho player
     */
    public void unlockTitle(UUID uuid, String titleId, Role role) throws SQLException {
        String sql = """
            INSERT INTO role_titles (uuid, title_id, role, unlocked_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid, title_id) DO NOTHING
        """;
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, titleId);
            stmt.setString(3, role.name());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }

    /**
     * Check xem player đã unlock danh hiệu chưa
     */
    public boolean hasTitle(UUID uuid, String titleId) throws SQLException {
        String sql = "SELECT 1 FROM role_titles WHERE uuid = ? AND title_id = ? LIMIT 1";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, titleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Lấy danh hiệu đang active của player
     */
    public String getActiveTitle(UUID uuid) throws SQLException {
        String sql = "SELECT title_id FROM role_active_title WHERE uuid = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title_id");
                }
            }
        }
        return null;
    }

    /**
     * Set danh hiệu active cho player
     */
    public void setActiveTitle(UUID uuid, String titleId) throws SQLException {
        String sql = """
            INSERT INTO role_active_title (uuid, title_id)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET title_id = excluded.title_id
        """;
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, titleId);
            stmt.executeUpdate();
        }
    }

    /**
     * Remove danh hiệu active (set về null)
     */
    public void removeActiveTitle(UUID uuid) throws SQLException {
        String sql = "DELETE FROM role_active_title WHERE uuid = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }
}
