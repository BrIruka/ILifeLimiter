package com.iruka.lifelimiter.listeners;

import com.iruka.lifelimiter.ILifeLimiter;
import com.iruka.lifelimiter.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final ILifeLimiter plugin;

    public PlayerDeathListener(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getEntity();
            UUID playerUUID = player.getUniqueId();
            int currentHearts = plugin.getPlayerHearts(playerUUID);

            if (currentHearts > 0) {
                plugin.removeHeart(playerUUID);
                int newHearts = plugin.getPlayerHearts(playerUUID);

                String deathMessage = plugin.getLanguageManager().getMessage("messages.death")
                        .replace("%hearts%", String.valueOf(newHearts));
                player.sendMessage(ColorUtils.colorize(deathMessage));

                if (newHearts <= 0) {
                    String banReason = ColorUtils.colorize(
                            plugin.getLanguageManager().getMessage("messages.ban-reason"));

                    String banCommand = plugin.getConfig().getString("commands.ban-command")
                            .replace("%player%", player.getName())
                            .replace("%reason%", banReason);

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
                }
            }
        }, 1L);
    }
}