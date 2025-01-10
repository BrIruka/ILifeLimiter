package com.iruka.lifelimiter.listeners;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final ILifeLimiter plugin;

    public PlayerQuitListener(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getCacheManager().savePlayer(event.getPlayer().getUniqueId());
    }
}