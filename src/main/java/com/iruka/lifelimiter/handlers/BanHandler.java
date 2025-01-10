package com.iruka.lifelimiter.handlers;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanHandler {
    private final ILifeLimiter plugin;

    public BanHandler(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    public void checkAndBanIfNeeded(UUID playerUUID) {
        int hearts = plugin.getPlayerHearts(playerUUID);

        // Строгая проверка на 0
        if (hearts == 0) {
            plugin.getLogger().info("[BanHandler] Условие для бана выполнено");
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                String banReason = ChatColor.translateAlternateColorCodes('&',
                        plugin.getLanguageManager().getMessage("messages.ban-reason"));

                String banCommand = plugin.getConfig().getString("commands.ban-command")
                        .replace("%player%", player.getName())
                        .replace("%reason%", banReason);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
                });
            }
        }
    }
}