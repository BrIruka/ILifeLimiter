package com.iruka.lifelimiter.commands;

import com.iruka.lifelimiter.ILifeLimiter;
import com.iruka.lifelimiter.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LivesCommandExecutor implements CommandExecutor {
    private final ILifeLimiter plugin;

    public LivesCommandExecutor(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lifelimiter.admin")) {
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.no-permission")));
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelpMessage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.colorize(
                        plugin.getLanguageManager().getMessage("messages.usage")));
                return true;
            }

            String itemId = args[1];
            String playerName = args[2];
            Player target = Bukkit.getPlayer(playerName);

            if (target == null) {
                sender.sendMessage(ColorUtils.colorize(
                        plugin.getLanguageManager().getMessage("messages.player-not-found")));
                return true;
            }

            if (itemId.equalsIgnoreCase("life-stealer")) {
                ItemStack lifeStealer = plugin.getItemManager().createLifeStealer();
                target.getInventory().addItem(lifeStealer);
                target.sendMessage(ColorUtils.colorize(
                        plugin.getLanguageManager().getMessage("messages.life-stealer-given")));
                return true;
            }

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.config-reloaded")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.usage")));
            return true;
        }

        String subCommand = args[0];
        String playerName = args[1];
        int hearts;

        try {
            hearts = Integer.parseInt(args[2]);
            if (hearts <= 0) {
                sender.sendMessage(ColorUtils.colorize(
                        plugin.getLanguageManager().getMessage("messages.invalid-hearts-amount")));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.invalid-hearts-number")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.player-not-found")));
            return true;
        }

        int maxHearts = plugin.getConfig().getInt("settings.max-hearts");
        int currentHearts = plugin.getPlayerHearts(target.getUniqueId());

        switch (subCommand.toLowerCase()) {
            case "add":
                if (currentHearts + hearts > maxHearts) {
                    int actualAdded = maxHearts - currentHearts;
                    plugin.setPlayerHearts(target.getUniqueId(), maxHearts);

                    if (target.isOnline()) {
                        plugin.updatePlayerHealth(target.getPlayer());
                    }

                    sender.sendMessage(ColorUtils.colorize(
                            plugin.getLanguageManager().getMessage("messages.hearts-added-max")
                                    .replace("%player%", playerName)
                                    .replace("%hearts%", String.valueOf(actualAdded))
                                    .replace("%max%", String.valueOf(maxHearts))));
                    return true;
                }
                plugin.setPlayerHearts(target.getUniqueId(), currentHearts + hearts);

                if (target.isOnline()) {
                    plugin.updatePlayerHealth(target.getPlayer());
                }
                break;

            case "remove":
                if (currentHearts - hearts < plugin.getConfig().getInt("settings.min-hearts")) {
                    sender.sendMessage(ColorUtils.colorize(
                            plugin.getLanguageManager().getMessage("messages.not-enough-hearts")));
                    return true;
                }
                plugin.setPlayerHearts(target.getUniqueId(), currentHearts - hearts);

                if (target.isOnline()) {
                    plugin.updatePlayerHealth(target.getPlayer());
                }
                break;

            case "unban":
                if (!plugin.getDatabase().isPlayerBanned(target.getUniqueId())) {
                    sender.sendMessage(ColorUtils.colorize(
                            plugin.getLanguageManager().getMessage("messages.player-not-banned")));
                    return true;
                }

                String unbanCommand = plugin.getConfig().getString("commands.unban-command")
                        .replace("%player%", playerName);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unbanCommand);
                plugin.setPlayerHearts(target.getUniqueId(), hearts);

                if (target.isOnline()) {
                    plugin.updatePlayerHealth(target.getPlayer());
                }
                break;

            default:
                return false;
        }

        String messageKey = switch (subCommand.toLowerCase()) {
            case "add" -> "messages.hearts-added";
            case "remove" -> "messages.hearts-removed";
            case "unban" -> "messages.player-revived";
            default -> null;
        };

        if (messageKey != null) {
            sender.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage(messageKey)
                            .replace("%player%", playerName)
                            .replace("%hearts%", String.valueOf(hearts))));
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.header")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.add")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.give")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.remove")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.unban")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.reload")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.help")));
        sender.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("help.footer")));
    }
}