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
                // Chưa có role, mở role select GUI (sẽ implement sau)
                player.sendMessage("§eSử dụng: /role select <TANKER|DPS|HEALER>");
            } else {
                // Đã có role, mở role info GUI
                me.skibidi.rolemmo.gui.RoleInfoGUI.open(player, plugin);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select" -> {
                if (args.length < 2) {
                    player.sendMessage("§cSử dụng: /role select <TANKER|DPS|HEALER>");
                    return true;
                }

                try {
                    Role role = Role.valueOf(args[1].toUpperCase());
                    if (roleManager.selectRole(player, role)) {
                        // Mở GUI role info sau khi chọn
                        me.skibidi.rolemmo.gui.RoleInfoGUI.open(player, plugin);
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cRole không hợp lệ! Các role: TANKER, DPS, HEALER");
                }
            }

            case "info" -> {
                Role currentRole = roleManager.getPlayerRole(player);
                if (currentRole == null) {
                    player.sendMessage("§cBạn chưa chọn role! Sử dụng /role select <role>");
                } else {
                    me.skibidi.rolemmo.gui.RoleInfoGUI.open(player, plugin);
                }
            }
            
            case "titles" -> {
                // Mở Title GUI
                me.skibidi.rolemmo.gui.TitleGUI.open(player, plugin);
            }

            case "change" -> {
                if (args.length < 2) {
                    player.sendMessage("§cSử dụng: /role change <TANKER|DPS|HEALER>");
                    return true;
                }

                try {
                    Role newRole = Role.valueOf(args[1].toUpperCase());
                    roleManager.changeRole(player, newRole);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cRole không hợp lệ! Các role: TANKER, DPS, HEALER");
                }
            }

            default -> {
                player.sendMessage("§cLệnh không hợp lệ!");
                player.sendMessage("§e/role select <role> - Chọn role");
                player.sendMessage("§e/role info - Xem thông tin role");
                player.sendMessage("§e/role change <role> - Đổi role");
            }
        }

        return true;
    }
}
