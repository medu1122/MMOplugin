package me.skibidi.rolemmo.util;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.config.ConfigManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

/**
 * Utility class để xử lý các thao tác liên quan đến LuckPerms folder
 * Folder LuckPerms sẽ được copy vào server, class này giúp reference và copy config nếu cần
 */
public class LuckPermsUtil {

    private final ROLEmmo plugin;
    private final ConfigManager configManager;
    private final Logger logger;

    public LuckPermsUtil(ROLEmmo plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.logger = plugin.getLogger();
    }

    /**
     * Lấy đường dẫn đến folder LuckPerms từ config
     * Nếu không config thì trả về null
     */
    public String getLuckPermsFolderPath() {
        return configManager.getLuckPermsFolderPath();
    }

    /**
     * Lấy File object của LuckPerms folder
     * @return File object hoặc null nếu không tìm thấy
     */
    public File getLuckPermsFolder() {
        String path = getLuckPermsFolderPath();
        if (path == null || path.isEmpty()) {
            return null;
        }

        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            return folder;
        }

        // Nếu path không tồn tại, thử tìm trong server root
        File serverRoot = plugin.getServer().getWorldContainer();
        File luckPermsInServer = new File(serverRoot, "plugins/LuckPerms");
        if (luckPermsInServer.exists() && luckPermsInServer.isDirectory()) {
            logger.info("Found LuckPerms folder at: " + luckPermsInServer.getAbsolutePath());
            return luckPermsInServer;
        }

        logger.warning("LuckPerms folder not found at configured path: " + path);
        return null;
    }

    /**
     * Copy file từ source folder vào LuckPerms folder
     * @param sourceFile File cần copy (relative path từ plugin data folder hoặc absolute path)
     * @param targetFileName Tên file đích trong LuckPerms folder
     * @return true nếu copy thành công
     */
    public boolean copyFileToLuckPerms(File sourceFile, String targetFileName) {
        File luckPermsFolder = getLuckPermsFolder();
        if (luckPermsFolder == null) {
            logger.warning("Cannot copy file to LuckPerms: folder not found");
            return false;
        }

        if (!sourceFile.exists()) {
            logger.warning("Source file does not exist: " + sourceFile.getAbsolutePath());
            return false;
        }

        try {
            File targetFile = new File(luckPermsFolder, targetFileName);
            Path sourcePath = sourceFile.toPath();
            Path targetPath = targetFile.toPath();

            // Tạo parent directories nếu cần
            if (targetFile.getParentFile() != null) {
                targetFile.getParentFile().mkdirs();
            }

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied file to LuckPerms: " + targetFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.severe("Failed to copy file to LuckPerms: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy config file từ plugin data folder vào LuckPerms folder
     * @param configFileName Tên file config trong plugin data folder
     * @param targetFileName Tên file đích trong LuckPerms folder
     * @return true nếu copy thành công
     */
    public boolean copyConfigToLuckPerms(String configFileName, String targetFileName) {
        File sourceFile = new File(plugin.getDataFolder(), configFileName);
        return copyFileToLuckPerms(sourceFile, targetFileName);
    }

    /**
     * Kiểm tra xem LuckPerms folder có tồn tại không
     */
    public boolean isLuckPermsFolderAvailable() {
        return getLuckPermsFolder() != null;
    }
}
