package me.skibidi.rolemmo.model;

/**
 * Model class cho Title (Danh hiệu)
 */
public class Title {
    private final String id;
    private final String name;
    private final Role role;
    private final int requiredLevel;

    public Title(String id, String name, Role role, int requiredLevel) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Title ID cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Title name cannot be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (requiredLevel < 1 || requiredLevel > 999) {
            throw new IllegalArgumentException("Required level must be between 1 and 999: " + requiredLevel);
        }

        this.id = id;
        this.name = name;
        this.role = role;
        this.requiredLevel = requiredLevel;
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

    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * Kiểm tra xem title có thể unlock ở level này không
     */
    public boolean canUnlockAt(int level) {
        return level >= requiredLevel;
    }

    /**
     * Lấy display name với màu sắc của role
     */
    public String getDisplayName() {
        return role.getColor() + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title = (Title) o;
        return id.equals(title.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Title{id='" + id + "', name='" + name + "', role=" + role + ", requiredLevel=" + requiredLevel + "}";
    }
}
