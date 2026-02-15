package me.skibidi.rolemmo.command;

import me.skibidi.rolemmo.model.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer cho RoleAdminCommand
 */
public class RoleAdminTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rolemmo.admin")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("givelevel", "giveskillpoints", "setrole", "takeskill", "giveexp", "setlevel");
            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            // Player names
            String input = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("givelevel") || subCommand.equals("setrole") || subCommand.equals("giveexp")) {
                // Suggest roles
                String input = args[2].toUpperCase();
                for (Role role : Role.values()) {
                    if (role.name().startsWith(input)) {
                        completions.add(role.name().toLowerCase());
                    }
                }
            } else if (subCommand.equals("takeskill")) {
                // Suggest skill IDs (có thể mở rộng sau)
                List<String> skillIds = Arrays.asList("fireball");
                String input = args[2].toLowerCase();
                for (String skillId : skillIds) {
                    if (skillId.startsWith(input)) {
                        completions.add(skillId);
                    }
                }
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("givelevel") || subCommand.equals("giveexp")) {
                // Suggest numbers
                String input = args[3];
                if (input.isEmpty()) {
                    completions.add("1");
                    completions.add("10");
                    completions.add("100");
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }
}
