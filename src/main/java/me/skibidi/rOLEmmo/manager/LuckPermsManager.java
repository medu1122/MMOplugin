package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.model.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.DisplayNameNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.WeightNode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manager để tương tác với LuckPerms API
 * Tự động tạo groups và set groups cho players
 */
public class LuckPermsManager {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private LuckPerms api;
    private boolean enabled = false;

    public LuckPermsManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        initialize();
    }

    private void initialize() {
        try {
            // Kiểm tra xem LuckPerms có sẵn không
            Plugin luckPermsPlugin = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
            if (luckPermsPlugin != null && luckPermsPlugin.isEnabled()) {
                api = LuckPermsProvider.get();
                enabled = true;
                plugin.getLogger().info("LuckPerms API initialized successfully!");
                
                // Tạo groups khi khởi động
                createGroupsIfNotExist();
            } else {
                plugin.getLogger().info("LuckPerms not found! Role groups will not be set automatically.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize LuckPerms API: " + e.getMessage());
            enabled = false;
        }
    }

    /**
     * Kiểm tra xem LuckPerms có sẵn không
     */
    public boolean isEnabled() {
        return enabled && api != null;
    }

    /**
     * Tạo groups cho các role nếu chưa tồn tại
     */
    private void createGroupsIfNotExist() {
        if (!isEnabled()) return;

        for (Role role : Role.values()) {
            String groupName = configManager.getLuckPermsGroup(role);
            createGroupIfNotExist(groupName, role);
        }
    }

    /**
     * Tạo group nếu chưa tồn tại với đầy đủ permissions, prefix, weight
     */
    private void createGroupIfNotExist(String groupName, Role role) {
        if (!isEnabled()) return;

        CompletableFuture<Group> groupFuture = api.getGroupManager().loadGroup(groupName);
        groupFuture.thenAcceptAsync(group -> {
            try {
                if (group == null) {
                    // Group chưa tồn tại, tạo mới
                    Group newGroup = api.getGroupManager().createAndLoadGroup(groupName).join();
                    if (newGroup != null) {
                        // Set weight dựa trên role
                        int weight = getWeightForRole(role);
                        newGroup.data().add(WeightNode.builder()
                                .weight(weight)
                                .build());

                        // Set prefix
                        String prefix = role.getColor() + "[" + role.getDisplayName() + "]";
                        newGroup.data().add(PrefixNode.builder()
                                .prefix(prefix)
                                .priority(weight)
                                .build());

                        // Set display name
                        newGroup.data().add(DisplayNameNode.builder()
                                .displayName(role.getFullDisplayName())
                                .build());

                        // Add permissions
                        newGroup.data().add(net.luckperms.api.node.Node.builder("rolemmo.use").build());
                        newGroup.data().add(net.luckperms.api.node.Node.builder("rolemmo." + role.name().toLowerCase() + ".*").build());

                        // Save group
                        api.getGroupManager().saveGroup(newGroup);
                        plugin.getLogger().info("Created LuckPerms group: " + groupName + " for role: " + role.name() + 
                                " with weight: " + weight);
                    }
                } else {
                    // Group đã tồn tại, đảm bảo có đầy đủ permissions
                    ensureGroupPermissions(group, role);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create/update LuckPerms group " + groupName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Error loading group " + groupName + ": " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Đảm bảo group có đầy đủ permissions
     */
    private void ensureGroupPermissions(Group group, Role role) {
        try {
            boolean hasUsePermission = group.data().contains(net.luckperms.api.node.Node.builder("rolemmo.use").build());
            boolean hasRolePermission = group.data().contains(
                    net.luckperms.api.node.Node.builder("rolemmo." + role.name().toLowerCase() + ".*").build());

            if (!hasUsePermission || !hasRolePermission) {
                if (!hasUsePermission) {
                    group.data().add(net.luckperms.api.node.Node.builder("rolemmo.use").build());
                }
                if (!hasRolePermission) {
                    group.data().add(net.luckperms.api.node.Node.builder("rolemmo." + role.name().toLowerCase() + ".*").build());
                }
                api.getGroupManager().saveGroup(group);
                plugin.getLogger().info("Updated permissions for group: " + group.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to ensure permissions for group " + group.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Lấy weight cho role (để set priority trong LuckPerms)
     */
    private int getWeightForRole(Role role) {
        return switch (role) {
            case TANKER -> 10;
            case DPS -> 20;
            case HEALER -> 30;
        };
    }

    /**
     * Set group cho player trong LuckPerms
     */
    public void setPlayerGroup(Player player, Role role) {
        if (!isEnabled()) {
            plugin.getLogger().warning("Cannot set group: LuckPerms API not available");
            return;
        }

        String groupName = configManager.getLuckPermsGroup(role);
        UUID uuid = player.getUniqueId();

        // Đảm bảo group tồn tại trước
        createGroupIfNotExist(groupName, role);

        // Load user
        CompletableFuture<User> userFuture = api.getUserManager().loadUser(uuid);
        userFuture.thenAcceptAsync(user -> {
            try {
                if (user == null) {
                    plugin.getLogger().warning("Failed to load user: " + player.getName());
                    return;
                }

                // Remove old role groups
                for (Role r : Role.values()) {
                    String oldGroupName = configManager.getLuckPermsGroup(r);
                    if (!oldGroupName.equals(groupName)) {
                        InheritanceNode oldNode = InheritanceNode.builder(oldGroupName).build();
                        if (user.data().contains(oldNode)) {
                            user.data().remove(oldNode);
                        }
                    }
                }

                // Add new group (chỉ add nếu chưa có)
                InheritanceNode groupNode = InheritanceNode.builder(groupName).build();
                if (!user.data().contains(groupNode)) {
                    user.data().add(groupNode);
                }

                // Save user
                api.getUserManager().saveUser(user);
                plugin.getLogger().info("Set group " + groupName + " for player: " + player.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to set group for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Error setting group for player " + player.getName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Remove group khỏi player
     */
    public void removePlayerGroup(Player player, Role role) {
        if (!isEnabled()) return;

        String groupName = configManager.getLuckPermsGroup(role);
        UUID uuid = player.getUniqueId();

        CompletableFuture<User> userFuture = api.getUserManager().loadUser(uuid);
        userFuture.thenAcceptAsync(user -> {
            if (user != null) {
                user.data().remove(InheritanceNode.builder(groupName).build());
                api.getUserManager().saveUser(user);
            }
        });
    }

    /**
     * Get LuckPerms API instance
     */
    public LuckPerms getApi() {
        return api;
    }
}
