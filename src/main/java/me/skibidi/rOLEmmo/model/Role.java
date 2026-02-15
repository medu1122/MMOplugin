package me.skibidi.rolemmo.model;

public enum Role {
    TANKER("Tanker", "§6", "🛡"),
    DPS("DPS", "§c", "⚔"),
    HEALER("Healer", "§a", "✝");

    private final String displayName;
    private final String color;
    private final String icon;

    Role(String displayName, String color, String icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public String getFullDisplayName() {
        return color + icon + " " + displayName;
    }
}
