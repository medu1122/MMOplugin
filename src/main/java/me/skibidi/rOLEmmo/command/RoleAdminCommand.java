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
                if (target == null || !target.isOnline()) {
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
                        if (target.isOnline()) {
                            target.sendMessage("§aAdmin đã set level " + level + " cho role " + role.getDisplayName() + " của bạn!");
                        }
                    } else {
                        sender.sendMessage("§cLỗi khi set level! Vui lòng thử lại sau.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLevel phải là số!");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid_role"));
                }
            }

            case "giveskillpoints" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cSử dụng: /roleadmin giveskillpoints <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    int points = Integer.parseInt(args[2]);
                    
                    // Validate points (tránh quá lớn hoặc quá nhỏ)
                    if (points < -1000000 || points > 1000000) {
                        sender.sendMessage("§cAmount phải từ -1,000,000 đến 1,000,000!");
                        return true;
                    }
                    
                    roleManager.addSkillPoints(target, points);

                    sender.sendMessage(plugin.getConfigManager().getMessage("admin_give_skill_points")
                            .replace("{player}", target.getName())
                            .replace("{points}", String.valueOf(points)));
                    if (target.isOnline()) {
                        target.sendMessage("§aAdmin đã give " + points + " skill points cho bạn!");
                    }
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
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    Role role = Role.valueOf(args[2].toUpperCase());
                    
                    // Check nếu player đã có role, dùng forceChangeRole (bypass cooldown cho admin)
                    Role currentRole = roleManager.getPlayerRole(target);
                    if (currentRole != null && currentRole != role) {
                        // Force change role (admin bypass cooldown)
                        if (roleManager.forceChangeRole(target, role)) {
                            sender.sendMessage(plugin.getConfigManager().getMessage("admin_set_role")
                                    .replace("{player}", target.getName())
                                    .replace("{role}", role.getFullDisplayName()));
                            if (target.isOnline()) {
                                target.sendMessage("§aAdmin đã set role " + role.getFullDisplayName() + " cho bạn!");
                            }
                        } else {
                            sender.sendMessage("§cLỗi khi set role! Vui lòng thử lại sau.");
                        }
                    } else if (currentRole == null) {
                        // Chưa có role, dùng selectRole
                        if (roleManager.selectRole(target, role)) {
                            sender.sendMessage(plugin.getConfigManager().getMessage("admin_set_role")
                                    .replace("{player}", target.getName())
                                    .replace("{role}", role.getFullDisplayName()));
                            if (target.isOnline()) {
                                target.sendMessage("§aAdmin đã set role " + role.getFullDisplayName() + " cho bạn!");
                            }
                        } else {
                            sender.sendMessage("§cLỗi khi set role! Vui lòng thử lại sau.");
                        }
                    } else {
                        sender.sendMessage("§cPlayer đã có role này rồi!");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid_role"));
                }
            }

            case "takeskill" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cSử dụng: /roleadmin takeskill <player> <skillId>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                String skillId = args[2];
                if (target.isOnline()) {
                    me.skibidi.rolemmo.util.SkillItemUtil.removeSkillItem(target, skillId);
                }
                
                sender.sendMessage("§aĐã remove skill item " + skillId + " khỏi " + target.getName());
                if (target.isOnline()) {
                    target.sendMessage("§cAdmin đã remove skill item " + skillId + " của bạn!");
                }
            }

            case "giveexp" -> {
                if (args.length < 4) {
                    sender.sendMessage("§cSử dụng: /roleadmin giveexp <player> <role> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player_not_found"));
                    return true;
                }

                try {
                    Role role = Role.valueOf(args[2].toUpperCase());
                    int exp = Integer.parseInt(args[3]);

                    if (exp < 0) {
                        sender.sendMessage("§cExp phải >= 0!");
                        return true;
                    }

                    // Validate exp amount (tránh quá lớn)
                    if (exp > 10000000) {
                        sender.sendMessage("§cExp quá lớn! Tối đa 10,000,000.");
                        return true;
                    }

                    if (plugin.getRoleManager().getPlayerRole(target) == null) {
                        sender.sendMessage("§cPlayer chưa có role! Hãy cho họ chọn role trước.");
                        return true;
                    }

                    plugin.getLevelManager().addExperience(target, role, exp);
                    
                    sender.sendMessage("§aĐã give " + exp + " exp cho role " + role.getDisplayName() + " của " + target.getName());
                    if (target.isOnline()) {
                        target.sendMessage("§aAdmin đã give " + exp + " exp cho role " + role.getDisplayName() + " của bạn!");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount phải là số!");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid_role"));
                }
            }

            default -> {
                sender.sendMessage("§cLệnh không hợp lệ!");
                sender.sendMessage("§e/roleadmin givelevel <player> <role> <level>");
                sender.sendMessage("§e/roleadmin giveskillpoints <player> <amount>");
                sender.sendMessage("§e/roleadmin setrole <player> <role>");
                sender.sendMessage("§e/roleadmin takeskill <player> <skillId>");
                sender.sendMessage("§e/roleadmin giveexp <player> <role> <amount>");
            }
        }

        return true;
    }
}
