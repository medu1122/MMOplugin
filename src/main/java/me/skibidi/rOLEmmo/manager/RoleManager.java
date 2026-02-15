package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.storage.DatabaseManager;
import me.skibidi.rolemmo.storage.repository.PlayerRoleRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manager chính để quản lý role system
 * Xử lý select role, change role, level, exp, skill points
 */
public class RoleManager {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final PlayerRoleRepository playerRoleRepository;
    private final LuckPermsManager luckPermsManager;
    private final MoneyPluginManager moneyPluginManager;
    private final Logger logger;

    public RoleManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.playerRoleRepository = new PlayerRoleRepository(databaseManager);
        this.luckPermsManager = new LuckPermsManager(plugin);
        this.moneyPluginManager = new MoneyPluginManager(plugin);
        this.logger = plugin.getLogger();
    }

    /**
     * Lấy role hiện tại của player
     */
    public Role getPlayerRole(Player player) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getCurrentRole() : null;
        } catch (SQLException e) {
            logger.severe("Failed to get player role: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra player đã có role chưa
     */
    public boolean hasRole(Player player) {
        return getPlayerRole(player) != null;
    }

    /**
     * Chọn role cho player (lần đầu)
     */
    public boolean selectRole(Player player, Role role) {
        if (player == null || role == null) {
            logger.warning("Invalid parameters for selectRole: player=" + player + ", role=" + role);
            return false;
        }

        if (hasRole(player)) {
            player.sendMessage(configManager.getMessage("role_already_selected"));
            return false;
        }

        try {
            // Kiểm tra xem player đã có data chưa
            PlayerRoleRepository.PlayerRoleData existingData = playerRoleRepository.getPlayerRole(player.getUniqueId());
            
            PlayerRoleRepository.PlayerRoleData data;
            if (existingData != null) {
                // Đã có data, chỉ update role
                data = existingData;
                data.setCurrentRole(role);
                data.setLastRoleChange(System.currentTimeMillis());
            } else {
                // Tạo player data mới
                data = new PlayerRoleRepository.PlayerRoleData(
                        player.getUniqueId(),
                        role,
                        1, 0, // tanker level, exp
                        1, 0, // dps level, exp
                        1, 0, // healer level, exp
                        0,    // skill points
                        System.currentTimeMillis() // last role change
                );
            }

            // Set level 1 cho role được chọn (nếu chưa có level)
            if (data.getLevel(role) < 1) {
                data.setLevel(role, 1);
                data.setExp(role, 0);
            }

            // Lưu vào database
            playerRoleRepository.savePlayerRole(data);

            // Set LuckPerms group (async, không block)
            if (luckPermsManager.isEnabled()) {
                luckPermsManager.setPlayerGroup(player, role);
            }

            player.sendMessage(configManager.getMessage("role_selected")
                    .replace("{role}", role.getFullDisplayName()));

            logger.info("Player " + player.getName() + " (" + player.getUniqueId() + ") selected role: " + role.name());
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to select role for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cLỗi khi chọn role! Vui lòng thử lại sau.");
            return false;
        } catch (Exception e) {
            logger.severe("Unexpected error in selectRole for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cLỗi không mong đợi! Vui lòng liên hệ admin.");
            return false;
        }
    }

    /**
     * Đổi role cho player
     */
    public boolean changeRole(Player player, Role newRole) {
        if (player == null || newRole == null) {
            logger.warning("Invalid parameters for changeRole: player=" + player + ", role=" + newRole);
            return false;
        }

        Role currentRole = getPlayerRole(player);
        if (currentRole == null) {
            // Chưa có role, dùng selectRole
            return selectRole(player, newRole);
        }

        if (currentRole == newRole) {
            player.sendMessage("§cBạn đã có role này rồi!");
            return false;
        }

        // Check cooldown và cost
        if (!canChangeRole(player)) {
            long cost = configManager.getRoleChangeCost();
            player.sendMessage(configManager.getMessage("role_change_cooldown")
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{time}", getTimeUntilCanChange(player)));
            return false;
        }

        // Nếu chưa đủ cooldown, check coins
        if (!canChangeRoleForFree(player)) {
            long cost = configManager.getRoleChangeCost();
            if (cost <= 0) {
                logger.warning("Invalid role change cost: " + cost);
                player.sendMessage("§cLỗi cấu hình! Vui lòng liên hệ admin.");
                return false;
            }

            if (!moneyPluginManager.isEnabled()) {
                player.sendMessage("§cHệ thống coins không khả dụng! Không thể đổi role.");
                return false;
            }

            if (!moneyPluginManager.hasEnough(player, cost)) {
                player.sendMessage(configManager.getMessage("role_change_not_enough_coins")
                        .replace("{cost}", String.valueOf(cost)));
                return false;
            }

            // Trừ coins
            if (!moneyPluginManager.removeCoins(player, cost)) {
                player.sendMessage("§cLỗi khi trừ coins! Vui lòng thử lại sau.");
                return false;
            }
            logger.info("Player " + player.getName() + " paid " + cost + " coins to change role");
        }

        try {
            // Lấy data hiện tại
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) {
                logger.warning("Player data not found for " + player.getName() + ", creating new...");
                return selectRole(player, newRole);
            }

            // Validate data
            if (data.getUuid() == null || !data.getUuid().equals(player.getUniqueId())) {
                logger.severe("Data UUID mismatch for player " + player.getName());
                player.sendMessage("§cLỗi dữ liệu! Vui lòng liên hệ admin.");
                return false;
            }

            // Update role
            data.setCurrentRole(newRole);
            data.setLastRoleChange(System.currentTimeMillis());

            // Lưu vào database
            playerRoleRepository.savePlayerRole(data);

            // Update LuckPerms group (async, không block)
            if (luckPermsManager.isEnabled()) {
                luckPermsManager.setPlayerGroup(player, newRole);
            }

            player.sendMessage(configManager.getMessage("role_change_success")
                    .replace("{role}", newRole.getFullDisplayName()));

            logger.info("Player " + player.getName() + " (" + player.getUniqueId() + ") changed role from " + 
                    currentRole.name() + " to " + newRole.name());
            return true;
        } catch (SQLException e) {
            logger.severe("Database error in changeRole for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cLỗi database! Vui lòng thử lại sau.");
            return false;
        } catch (Exception e) {
            logger.severe("Unexpected error in changeRole for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cLỗi không mong đợi! Vui lòng liên hệ admin.");
            return false;
        }
    }

    /**
     * Kiểm tra player có thể đổi role không (đã đủ cooldown)
     */
    public boolean canChangeRole(Player player) {
        return canChangeRoleForFree(player) || moneyPluginManager.isEnabled();
    }

    /**
     * Kiểm tra player có thể đổi role miễn phí không (đã đủ cooldown)
     */
    public boolean canChangeRoleForFree(Player player) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) return true; // Chưa có role, có thể chọn miễn phí

            long lastChange = data.getLastRoleChange();
            long cooldown = configManager.getRoleChangeCooldown();
            long timeSinceLastChange = System.currentTimeMillis() - lastChange;

            return timeSinceLastChange >= cooldown;
        } catch (SQLException e) {
            logger.warning("Failed to check role change cooldown: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thời gian còn lại cho đến khi có thể đổi role miễn phí
     */
    public String getTimeUntilCanChange(Player player) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data == null) return "0 giây";

            long lastChange = data.getLastRoleChange();
            long cooldown = configManager.getRoleChangeCooldown();
            long timeSinceLastChange = System.currentTimeMillis() - lastChange;
            long timeRemaining = cooldown - timeSinceLastChange;

            if (timeRemaining <= 0) return "0 giây";

            long hours = timeRemaining / (1000 * 60 * 60);
            long minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60);

            if (hours > 0) {
                return hours + " giờ " + minutes + " phút";
            } else {
                return minutes + " phút";
            }
        } catch (SQLException e) {
            return "Lỗi";
        }
    }

    /**
     * Lấy level của role cho player
     */
    public int getRoleLevel(Player player, Role role) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getLevel(role) : 1;
        } catch (SQLException e) {
            logger.warning("Failed to get role level: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Lấy exp của role cho player
     */
    public int getRoleExp(Player player, Role role) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getExp(role) : 0;
        } catch (SQLException e) {
            logger.warning("Failed to get role exp: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy skill points của player
     */
    public int getSkillPoints(Player player) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            return data != null ? data.getSkillPoints() : 0;
        } catch (SQLException e) {
            logger.warning("Failed to get skill points: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Thêm skill points cho player
     */
    public void addSkillPoints(Player player, int points) {
        try {
            PlayerRoleRepository.PlayerRoleData data = playerRoleRepository.getPlayerRole(player.getUniqueId());
            if (data != null) {
                data.setSkillPoints(data.getSkillPoints() + points);
                playerRoleRepository.savePlayerRole(data);
            }
        } catch (SQLException e) {
            logger.severe("Failed to add skill points: " + e.getMessage());
        }
    }

    // Getters cho managers
    public LuckPermsManager getLuckPermsManager() {
        return luckPermsManager;
    }

    public MoneyPluginManager getMoneyPluginManager() {
        return moneyPluginManager;
    }
}
