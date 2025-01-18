package com.iruka.lifelimiter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LivesTabCompleter implements TabCompleter {
    private final List<String> subCommands = List.of("add", "remove", "unban", "reload", "help", "give");
    private final List<String> items = List.of("life-stealer");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("lifelimiter.admin")) {
            return completions;
        }

        switch (args.length) {
            case 1 -> {
                return subCommands.stream()
                        .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("give")) {
                    return items.stream()
                            .filter(item -> item.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (!args[0].equalsIgnoreCase("reload")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("give")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (!args[0].equalsIgnoreCase("reload")) {
                    List<String> numbers = List.of("1", "5", "10");
                    return numbers.stream()
                            .filter(num -> num.startsWith(args[2]))
                            .collect(Collectors.toList());
                }
            }
        }

        return completions;
    }
}