package me.skibidi.rolemmo.storage.repository;

import me.skibidi.rolemmo.storage.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillRepository {

    private final DatabaseManager databaseManager;

    public SkillRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Lấy tất cả skill levels của player
     */
    public Map<String, Integer> getPlayerSkills(UUID uuid) throws SQLException {
        Map<String, Integer> skills = new HashMap<>();
        String sql = "SELECT skill_id, level FROM role_skills WHERE uuid = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    skills.put(rs.getString("skill_id"), rs.getInt("level"));
                }
            }
        }
        return skills;
    }

    /**
     * Lấy level của một skill cụ thể
     */
    public int getSkillLevel(UUID uuid, String skillId) throws SQLException {
        String sql = "SELECT level FROM role_skills WHERE uuid = ? AND skill_id = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, skillId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        }
        return 0; // Default level 0
    }

    /**
     * Set level của skill
     */
    public void setSkillLevel(UUID uuid, String skillId, int level) throws SQLException {
        String sql = """
            INSERT INTO role_skills (uuid, skill_id, level)
            VALUES (?, ?, ?)
            ON CONFLICT(uuid, skill_id) DO UPDATE SET level = excluded.level
        """;
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, skillId);
            stmt.setInt(3, level);
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa skill của player (nếu cần)
     */
    public void removeSkill(UUID uuid, String skillId) throws SQLException {
        String sql = "DELETE FROM role_skills WHERE uuid = ? AND skill_id = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, skillId);
            stmt.executeUpdate();
        }
    }
}
