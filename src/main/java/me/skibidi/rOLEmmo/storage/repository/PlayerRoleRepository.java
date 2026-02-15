package me.skibidi.rolemmo.storage.repository;

import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.storage.DatabaseManager;

import java.sql.*;
import java.util.UUID;

public class PlayerRoleRepository {

    private final DatabaseManager databaseManager;

    public PlayerRoleRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Lấy thông tin role của player từ database
     */
    public PlayerRoleData getPlayerRole(UUID uuid) throws SQLException {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        String sql = "SELECT * FROM role_players WHERE uuid = ?";
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String roleStr = rs.getString("current_role");
                    Role role = null;
                    if (roleStr != null && !roleStr.isEmpty()) {
                        try {
                            role = Role.valueOf(roleStr);
                        } catch (IllegalArgumentException e) {
                            // Invalid role in database, log warning
                            System.err.println("Warning: Invalid role in database for UUID " + uuid + ": " + roleStr);
                        }
                    }

                    // Check column existence bằng cách check ResultSetMetaData
                    String selectedSkillId = null;
                    long lastSkillChange = 0;
                    try {
                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        boolean hasSelectedSkillId = false;
                        boolean hasLastSkillChange = false;
                        
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            if ("selected_skill_id".equalsIgnoreCase(columnName)) {
                                hasSelectedSkillId = true;
                            }
                            if ("last_skill_change".equalsIgnoreCase(columnName)) {
                                hasLastSkillChange = true;
                            }
                        }
                        
                        if (hasSelectedSkillId) {
                            selectedSkillId = rs.getString("selected_skill_id");
                        }
                        if (hasLastSkillChange) {
                            lastSkillChange = rs.getLong("last_skill_change");
                        }
                    } catch (SQLException e) {
                        // Fallback: nếu không check được metadata, thử get trực tiếp
                        try {
                            selectedSkillId = rs.getString("selected_skill_id");
                        } catch (SQLException ignored) {
                            // Column không tồn tại
                        }
                        try {
                            lastSkillChange = rs.getLong("last_skill_change");
                        } catch (SQLException ignored) {
                            // Column không tồn tại
                        }
                    }
                    
                    return new PlayerRoleData(
                        UUID.fromString(rs.getString("uuid")),
                        role,
                        Math.max(1, rs.getInt("tanker_level")), // Ensure minimum level 1
                        Math.max(0, rs.getInt("tanker_exp")),   // Ensure non-negative
                        Math.max(1, rs.getInt("dps_level")),
                        Math.max(0, rs.getInt("dps_exp")),
                        Math.max(1, rs.getInt("healer_level")),
                        Math.max(0, rs.getInt("healer_exp")),
                        Math.max(0, rs.getInt("skill_points")),
                        Math.max(0, rs.getLong("last_role_change")),
                        selectedSkillId,
                        Math.max(0, lastSkillChange)
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting player role for UUID " + uuid + ": " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Tạo hoặc cập nhật thông tin role của player
     */
    public void savePlayerRole(PlayerRoleData data) throws SQLException {
        if (data == null) {
            throw new IllegalArgumentException("PlayerRoleData cannot be null");
        }
        if (data.getUuid() == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        // Validate data trước khi save
        validatePlayerRoleData(data);

        String sql = """
            INSERT INTO role_players (uuid, current_role, tanker_level, tanker_exp, dps_level, dps_exp, 
                                     healer_level, healer_exp, skill_points, last_role_change, selected_skill_id, last_skill_change)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                current_role = excluded.current_role,
                tanker_level = excluded.tanker_level,
                tanker_exp = excluded.tanker_exp,
                dps_level = excluded.dps_level,
                dps_exp = excluded.dps_exp,
                healer_level = excluded.healer_level,
                healer_exp = excluded.healer_exp,
                skill_points = excluded.skill_points,
                last_role_change = excluded.last_role_change,
                selected_skill_id = excluded.selected_skill_id,
                last_skill_change = excluded.last_skill_change
        """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getCurrentRole() != null ? data.getCurrentRole().name() : null);
            stmt.setInt(3, Math.max(1, data.getTankerLevel())); // Ensure minimum level 1
            stmt.setInt(4, Math.max(0, data.getTankerExp()));
            stmt.setInt(5, Math.max(1, data.getDpsLevel()));
            stmt.setInt(6, Math.max(0, data.getDpsExp()));
            stmt.setInt(7, Math.max(1, data.getHealerLevel()));
            stmt.setInt(8, Math.max(0, data.getHealerExp()));
            stmt.setInt(9, Math.max(0, data.getSkillPoints()));
            stmt.setLong(10, Math.max(0, data.getLastRoleChange()));
            stmt.setString(11, data.getSelectedSkillId()); // selected_skill_id
            stmt.setLong(12, Math.max(0, data.getLastSkillChange())); // last_skill_change
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to save player role data: no rows affected");
            }
        } catch (SQLException e) {
            System.err.println("Error saving player role for UUID " + data.getUuid() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Validate player role data trước khi save
     */
    private void validatePlayerRoleData(PlayerRoleData data) {
        // Validate levels (1-999)
        if (data.getTankerLevel() < 1 || data.getTankerLevel() > 999) {
            throw new IllegalArgumentException("Tanker level must be between 1 and 999: " + data.getTankerLevel());
        }
        if (data.getDpsLevel() < 1 || data.getDpsLevel() > 999) {
            throw new IllegalArgumentException("DPS level must be between 1 and 999: " + data.getDpsLevel());
        }
        if (data.getHealerLevel() < 1 || data.getHealerLevel() > 999) {
            throw new IllegalArgumentException("Healer level must be between 1 and 999: " + data.getHealerLevel());
        }

        // Validate exp (non-negative)
        if (data.getTankerExp() < 0 || data.getDpsExp() < 0 || data.getHealerExp() < 0) {
            throw new IllegalArgumentException("Experience cannot be negative");
        }

        // Validate skill points (non-negative)
        if (data.getSkillPoints() < 0) {
            throw new IllegalArgumentException("Skill points cannot be negative: " + data.getSkillPoints());
        }

        // Validate last role change (non-negative)
        if (data.getLastRoleChange() < 0) {
            throw new IllegalArgumentException("Last role change timestamp cannot be negative");
        }
    }

    /**
     * Data class cho player role information
     */
    public static class PlayerRoleData {
        private final UUID uuid;
        private Role currentRole;
        private int tankerLevel;
        private int tankerExp;
        private int dpsLevel;
        private int dpsExp;
        private int healerLevel;
        private int healerExp;
        private int skillPoints;
        private long lastRoleChange;
        private String selectedSkillId;
        private long lastSkillChange;

        public PlayerRoleData(UUID uuid, Role currentRole, int tankerLevel, int tankerExp,
                             int dpsLevel, int dpsExp, int healerLevel, int healerExp,
                             int skillPoints, long lastRoleChange, String selectedSkillId, long lastSkillChange) {
            this.uuid = uuid;
            this.currentRole = currentRole;
            this.tankerLevel = tankerLevel;
            this.tankerExp = tankerExp;
            this.dpsLevel = dpsLevel;
            this.dpsExp = dpsExp;
            this.healerLevel = healerLevel;
            this.healerExp = healerExp;
            this.skillPoints = skillPoints;
            this.lastRoleChange = lastRoleChange;
            this.selectedSkillId = selectedSkillId;
            this.lastSkillChange = lastSkillChange;
        }

        // Getters
        public UUID getUuid() { return uuid; }
        public Role getCurrentRole() { return currentRole; }
        public int getTankerLevel() { return tankerLevel; }
        public int getTankerExp() { return tankerExp; }
        public int getDpsLevel() { return dpsLevel; }
        public int getDpsExp() { return dpsExp; }
        public int getHealerLevel() { return healerLevel; }
        public int getHealerExp() { return healerExp; }
        public int getSkillPoints() { return skillPoints; }
        public long getLastRoleChange() { return lastRoleChange; }
        public String getSelectedSkillId() { return selectedSkillId; }
        public long getLastSkillChange() { return lastSkillChange; }

        // Setters
        public void setCurrentRole(Role currentRole) { this.currentRole = currentRole; }
        public void setTankerLevel(int tankerLevel) { this.tankerLevel = tankerLevel; }
        public void setTankerExp(int tankerExp) { this.tankerExp = tankerExp; }
        public void setDpsLevel(int dpsLevel) { this.dpsLevel = dpsLevel; }
        public void setDpsExp(int dpsExp) { this.dpsExp = dpsExp; }
        public void setHealerLevel(int healerLevel) { this.healerLevel = healerLevel; }
        public void setHealerExp(int healerExp) { this.healerExp = healerExp; }
        public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
        public void setLastRoleChange(long lastRoleChange) { this.lastRoleChange = lastRoleChange; }
        public void setSelectedSkillId(String selectedSkillId) { this.selectedSkillId = selectedSkillId; }
        public void setLastSkillChange(long lastSkillChange) { this.lastSkillChange = lastSkillChange; }

        /**
         * Lấy level của role cụ thể
         */
        public int getLevel(Role role) {
            return switch (role) {
                case TANKER -> tankerLevel;
                case DPS -> dpsLevel;
                case HEALER -> healerLevel;
            };
        }

        /**
         * Set level của role cụ thể
         */
        public void setLevel(Role role, int level) {
            switch (role) {
                case TANKER -> tankerLevel = level;
                case DPS -> dpsLevel = level;
                case HEALER -> healerLevel = level;
            }
        }

        /**
         * Lấy exp của role cụ thể
         */
        public int getExp(Role role) {
            return switch (role) {
                case TANKER -> tankerExp;
                case DPS -> dpsExp;
                case HEALER -> healerExp;
            };
        }

        /**
         * Set exp của role cụ thể
         */
        public void setExp(Role role, int exp) {
            switch (role) {
                case TANKER -> tankerExp = exp;
                case DPS -> dpsExp = exp;
                case HEALER -> healerExp = exp;
            }
        }
    }
}
