package me.skibidi.rolemmo.command;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.RoleManager;
import me.skibidi.rolemmo.model.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RoleAdminCommand implements CommandExecutor {

    private final ROLEmmo plugin;
    private final RoleManager roleManager;

    public RoleAdminCommand(ROLEmmo plugin) {
        this.plugin = plugin;
        this.roleManager = plugin.getRoleManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rolemmo.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cSử dụng:");
            sender.sendMessage("§e/roleadmin givelevel <player> <role> <level>");
            sender.sendMessage("§e/roleadmin giveskillpoints <player> <amount>");
            sender.sendMessage("§e/roleadmin setrole <player> <role>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "givelevel" -> {
                if (args.length < 4) {
                    sender.sendMessage("§cSử dụng: /roleadmin givelevel <player> <role> <level>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    Role role = Role.valueOf(args[2].toUpperCase());
                    int level = Integer.parseInt(args[3]);

                    if (level < 1 || level > 999) {
                        sender.sendMessage("§cLevel phải từ 1 đến 999!");
                        return true;
                    }

                    if (plugin.getRoleManager().getPlayerRole(target) == null) {
                        sender.sendMessage("§cPlayer chưa có role! Hãy cho họ chọn role trước.");
                        return true;
                    }

                    // Sử dụng LevelManager để set level (sẽ tự động unlock titles)
                    if (plugin.getLevelManager().setLevel(target, role, level)) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("admin_give_level")
                                .replace("{player}", target.getName())
                                .replace("{level}", String.valueOf(level)));
                        target.sendMessage("§aAdmin đã set level " + level + " cho role " + role.getDisplayName() + " của bạn!");
                    } else {
                        sender.sendMessage("§cLỗi khi set level! Vui lòng thử lại sau.");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid_role"));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLevel phải là số!");
                }
            }

            case "giveskillpoints" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cSử dụng: /roleadmin giveskillpoints <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    int points = Integer.parseInt(args[2]);
                    roleManager.addSkillPoints(target, points);

                    sender.sendMessage(plugin.getConfigManager().getMessage("admin_give_skill_points")
                            .replace("{player}", target.getName())
                            .replace("{points}", String.valueOf(points)));
                    target.sendMessage("§aAdmin đã give " + points + " skill points cho bạn!");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount phải là số!");
                }
            }

            case "setrole" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cSử dụng: /roleadmin setrole <player> <role>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    Role role = Role.valueOf(args[2].toUpperCase());
                    roleManager.selectRole(target, role);

                    sender.sendMessage(plugin.getConfigManager().getMessage("admin_set_role")
                            .replace("{player}", target.getName())
                            .replace("{role}", role.getFullDisplayName()));
                    target.sendMessage("§aAdmin đã set role " + role.getFullDisplayName() + " cho bạn!");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid_role"));
                }
            }

            default -> {
                sender.sendMessage("§cLệnh không hợp lệ!");
            }
        }

        return true;
    }
}
