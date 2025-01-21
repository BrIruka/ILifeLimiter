package com.iruka.lifelimiter.listeners;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerHealthManager implements Listener {
    private final ILifeLimiter plugin;

    public PlayerHealthManager(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDatabase().hasPlayerData(player.getUniqueId())) {
            int startingHearts = plugin.getConfig().getInt("settings.starting-hearts");
            plugin.setPlayerHearts(player.getUniqueId(), startingHearts);
        }

        updatePlayerHealth(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        updatePlayerHealth(event.getPlayer());
    }

    public void updatePlayerHealth(Player player) {
        int hearts = plugin.getPlayerHearts(player.getUniqueId());
        boolean modifyHearts = plugin.getConfig().getBoolean("settings.modify-hearts", true);
        double maxHealths = Math.max(2.0, hearts * 2.0);

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            if (modifyHearts) {
                attribute.setBaseValue(maxHealths);
                player.setHealth(Math.min(player.getHealth(), maxHealths));
            } else {
                double defaultHealth = 20.0; // Стандартное значение Minecraft (10 сердец)
                double modifiedHealth = attribute.getValue() - attribute.getBaseValue() + defaultHealth;
                attribute.setBaseValue(defaultHealth);
                player.setHealth(Math.min(player.getHealth(), modifiedHealth));
            }
        }
    }
}