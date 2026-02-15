package me.skibidi.rolemmo.command;

import me.skibidi.rolemmo.model.Role;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoleTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("select", "info", "change");
            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("select") || subCommand.equals("change")) {
                // Suggest roles
                String input = args[1].toUpperCase();
                for (Role role : Role.values()) {
                    if (role.name().startsWith(input)) {
                        completions.add(role.name().toLowerCase());
                    }
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }
}
