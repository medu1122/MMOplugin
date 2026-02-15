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
            List<String> subcommands = Arrays.asList("select", "info", "change", "titles");
            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }
}
