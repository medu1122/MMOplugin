package me.skibidi.rolemmo.command;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RoleCommand implements CommandExecutor {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;

    public RoleCommand(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            // Mở GUI role info hoặc role select
            Role currentRole = roleManager.getPlayerRole(player);
            if (currentRole == null) {
                // Chưa có role, mở role select GUI
                me.skibidi.rolemmo.gui.RoleSelectGUI.open(player, plugin);
            } else {
                // Đã có role, mở role info GUI
                me.skibidi.rolemmo.gui.RoleInfoGUI.open(player, plugin);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select" -> {
                // Mở GUI chọn role
                me.skibidi.rolemmo.gui.RoleSelectGUI.open(player, plugin);
            }
            
            case "change" -> {
                // Mở GUI đổi role
                me.skibidi.rolemmo.gui.RoleChangeGUI.open(player, plugin);
            }

            case "info" -> {
                Role currentRole = roleManager.getPlayerRole(player);
                if (currentRole == null) {
                    player.sendMessage("§cBạn chưa chọn role! Sử dụng /role select");
                } else {
                    me.skibidi.rolemmo.gui.RoleInfoGUI.open(player, plugin);
                }
            }
            
            case "titles" -> {
                // Mở Title GUI
                me.skibidi.rolemmo.gui.TitleGUI.open(player, plugin);
            }

            default -> {
                player.sendMessage("§cLệnh không hợp lệ!");
                player.sendMessage("§e/role - Mở GUI chính");
                player.sendMessage("§e/role select - Chọn role");
                player.sendMessage("§e/role info - Xem thông tin role");
                player.sendMessage("§e/role change - Đổi role");
                player.sendMessage("§e/role titles - Xem danh hiệu");
            }
        }

        return true;
    }
}
