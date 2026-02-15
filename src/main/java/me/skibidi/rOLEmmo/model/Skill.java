package me.skibidi.rolemmo.model;

import java.util.Map;

/**
 * Base class cho Skill
 * Mỗi skill sẽ extend class này
 */
public abstract class Skill {
    
    protected final String id;
    protected final String name;
    protected final Role role;
    protected final int maxLevel;

    public Skill(String id, String name, Role role, int maxLevel) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Skill ID cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (maxLevel < 1 || maxLevel > 6) {
            throw new IllegalArgumentException("Max level must be between 1 and 6: " + maxLevel);
        }

        this.id = id;
        this.name = name;
        this.role = role;
        this.maxLevel = maxLevel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Lấy mô tả của skill
     */
    public abstract String getDescription();

    /**
     * Lấy thông tin chi tiết của skill ở level cụ thể
     */
    public abstract SkillLevelInfo getLevelInfo(int level);

    /**
     * Execute skill (sẽ được implement bởi từng skill cụ thể)
     */
    public abstract boolean execute(org.bukkit.entity.Player player, int level);

    /**
     * Class để lưu thông tin skill level
     */
    public static class SkillLevelInfo {
        private final int level;
        private final int damage;
        private final int cooldown; // seconds
        private final Map<String, Object> properties;

        public SkillLevelInfo(int level, int damage, int cooldown, Map<String, Object> properties) {
            this.level = level;
            this.damage = damage;
            this.cooldown = cooldown;
            this.properties = properties != null ? Map.copyOf(properties) : Map.of();
        }

        public int getLevel() {
            return level;
        }

        public int getDamage() {
            return damage;
        }

        public int getCooldown() {
            return cooldown;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        @SuppressWarnings("unchecked")
        public <T> T getProperty(String key, Class<T> type) {
            Object value = properties.get(key);
            if (value != null && type.isInstance(value)) {
                return (T) value;
            }
            return null;
        }

        public int getPropertyInt(String key, int defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        }

        public double getPropertyDouble(String key, double defaultValue) {
            Object value = properties.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return defaultValue;
        }
    }
}
