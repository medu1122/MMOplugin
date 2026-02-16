package me.skibidi.rolemmo.manager;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Title;
import me.skibidi.rolemmo.storage.DatabaseManager;
import me.skibidi.rolemmo.storage.repository.TitleRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manager để quản lý Title System
 * Xử lý unlock, active, và lấy danh sách titles
 */
public class TitleManager {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private final TitleRepository titleRepository;
    private final Logger logger;
    
    // Cache titles từ config để tránh load lại nhiều lần (sử dụng ConcurrentHashMap để thread-safe)
    private final Map<Role, Map<Integer, Title>> titlesCache = new ConcurrentHashMap<>();

    public TitleManager(ROLEmmo plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.titleRepository = new TitleRepository(plugin.getDatabaseManager());
        this.logger = plugin.getLogger();
        loadTitlesFromConfig();
    }

    /**
     * Load tất cả titles từ config vào cache
     */
    private void loadTitlesFromConfig() {
        titlesCache.clear();
        for (Role role : Role.values()) {
            Map<Integer, String> titleMap = configManager.getTitlesForRole(role);
            Map<Integer, Title> roleTitles = new LinkedHashMap<>();
            
            for (Map.Entry<Integer, String> entry : titleMap.entrySet()) {
                int level = entry.getKey();
                String name = entry.getValue();
                String titleId = role.name().toLowerCase() + "_" + level;
                
                try {
                    Title title = new Title(titleId, name, role, level);
                    roleTitles.put(level, title);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid title config for " + role.name() + " level " + level + ": " + e.getMessage());
                }
            }
            
            titlesCache.put(role, roleTitles);
            logger.info("Loaded " + roleTitles.size() + " titles for role " + role.name());
        }
    }

    /**
     * Lấy tất cả titles của một role
     */
    public List<Title> getTitlesForRole(Role role) {
        Map<Integer, Title> roleTitles = titlesCache.get(role);
        if (roleTitles == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(roleTitles.values());
    }

    /**
     * Lấy title ở level cụ thể
     */
    public Title getTitleAtLevel(Role role, int level) {
        Map<Integer, Title> roleTitles = titlesCache.get(role);
        if (roleTitles == null) {
            return null;
        }
        return roleTitles.get(level);
    }

    /**
     * Lấy tất cả titles đã unlock của player (từ tất cả role)
     */
    public List<Title> getUnlockedTitles(Player player) {
        try {
            List<String> unlockedTitleIds = titleRepository.getUnlockedTitles(player.getUniqueId());
            List<Title> titles = new ArrayList<>();
            
            for (String titleId : unlockedTitleIds) {
                Title title = findTitleById(titleId);
                if (title != null) {
                    titles.add(title);
                }
            }
            
            return titles;
        } catch (SQLException e) {
            logger.severe("Failed to get unlocked titles for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy titles đã unlock của player theo role
     */
    public List<Title> getUnlockedTitlesByRole(Player player, Role role) {
        try {
            List<String> unlockedTitleIds = titleRepository.getUnlockedTitlesByRole(player.getUniqueId(), role);
            List<Title> titles = new ArrayList<>();
            
            for (String titleId : unlockedTitleIds) {
                Title title = findTitleById(titleId);
                if (title != null && title.getRole() == role) {
                    titles.add(title);
                }
            }
            
            return titles;
        } catch (SQLException e) {
            logger.severe("Failed to get unlocked titles for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Unlock title cho player (khi đạt level)
     */
    public boolean unlockTitle(Player player, Title title) {
        if (player == null || title == null) {
            logger.warning("Invalid parameters for unlockTitle: player=" + player + ", title=" + title);
            return false;
        }

        try {
            // Kiểm tra xem đã unlock chưa
            if (titleRepository.hasTitle(player.getUniqueId(), title.getId())) {
                return false; // Đã unlock rồi
            }

            // Unlock title
            titleRepository.unlockTitle(player.getUniqueId(), title.getId(), title.getRole());

            // Thông báo đạt danh hiệu: broadcast lên chat cho mọi người thấy (trừ khi chỉ có 1 message config)
            String broadcastMsg = configManager.getMessage("title_unlocked_broadcast", "§e{player} §ađã đạt danh hiệu §6{title}§a!")
                    .replace("{player}", player.getName())
                    .replace("{title}", title.getDisplayName());
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(broadcastMsg);
            }

            logger.info("Player " + player.getName() + " unlocked title: " + title.getId());
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to unlock title for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tự động unlock titles khi player level up
     */
    public void checkAndUnlockTitles(Player player, Role role, int newLevel) {
        if (player == null || role == null || newLevel < 1) {
            return;
        }

        // Check player online trước khi unlock titles
        if (!player.isOnline()) {
            return;
        }

        Map<Integer, Title> roleTitles = titlesCache.get(role);
        if (roleTitles == null) {
            return;
        }

        // Tìm tất cả titles có thể unlock ở level này
        for (Map.Entry<Integer, Title> entry : roleTitles.entrySet()) {
            int requiredLevel = entry.getKey();
            Title title = entry.getValue();
            
            // Nếu level mới đạt yêu cầu và chưa unlock
            if (newLevel >= requiredLevel) {
                try {
                    if (!titleRepository.hasTitle(player.getUniqueId(), title.getId())) {
                        // Double check player online trước khi unlock
                        if (player.isOnline()) {
                            unlockTitle(player, title);
                        }
                    }
                } catch (SQLException e) {
                    logger.warning("Failed to check title unlock for " + title.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Set active title cho player
     */
    public boolean setActiveTitle(Player player, Title title) {
        if (player == null) {
            return false;
        }

        // Check player online
        if (!player.isOnline()) {
            return false;
        }

        // Nếu title là null, remove active title
        if (title == null) {
            try {
                titleRepository.removeActiveTitle(player.getUniqueId());
                if (player.isOnline()) {
                    player.sendMessage("§aĐã gỡ danh hiệu active!");
                }
                return true;
            } catch (SQLException e) {
                logger.severe("Failed to remove active title for player " + player.getName() + ": " + e.getMessage());
                return false;
            }
        }

        // Kiểm tra xem player đã unlock title này chưa
        try {
            if (!titleRepository.hasTitle(player.getUniqueId(), title.getId())) {
                if (player.isOnline()) {
                    player.sendMessage("§cBạn chưa unlock danh hiệu này!");
                }
                return false;
            }

            // Set active title
            titleRepository.setActiveTitle(player.getUniqueId(), title.getId());
            if (player.isOnline()) {
                player.sendMessage("§aĐã set danh hiệu active: " + title.getDisplayName());
            }
            
            logger.info("Player " + player.getName() + " set active title: " + title.getId());
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to set active title for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            if (player.isOnline()) {
                player.sendMessage("§cLỗi khi set danh hiệu! Vui lòng thử lại sau.");
            }
            return false;
        }
    }

    /**
     * Lấy active title của player
     */
    public Title getActiveTitle(Player player) {
        if (player == null) {
            return null;
        }

        try {
            String activeTitleId = titleRepository.getActiveTitle(player.getUniqueId());
            if (activeTitleId == null) {
                return null;
            }
            return findTitleById(activeTitleId);
        } catch (SQLException e) {
            logger.warning("Failed to get active title for player " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Tìm title theo ID
     */
    private Title findTitleById(String titleId) {
        for (Map<Integer, Title> roleTitles : titlesCache.values()) {
            for (Title title : roleTitles.values()) {
                if (title.getId().equals(titleId)) {
                    return title;
                }
            }
        }
        return null;
    }

    /**
     * Reload titles từ config (khi config thay đổi)
     */
    public void reloadTitles() {
        loadTitlesFromConfig();
        logger.info("Titles reloaded from config");
    }
}
