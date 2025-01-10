package com.iruka.lifelimiter.placeholders;

import com.iruka.lifelimiter.ILifeLimiter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LivesExpansion extends PlaceholderExpansion {
    private final ILifeLimiter plugin;

    public LivesExpansion(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lifelimiter";
    }

    @Override
    public @NotNull String getAuthor() {
        return "iruka";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "hearts" -> String.valueOf(plugin.getPlayerHearts(player.getUniqueId()));
            case "max_hearts" -> String.valueOf(plugin.getConfig().getInt("settings.max-hearts"));
            case "min_hearts" -> String.valueOf(plugin.getConfig().getInt("settings.min-hearts"));
            case "is_banned" -> String.valueOf(plugin.getDatabase().isPlayerBanned(player.getUniqueId()));
            default -> null;
        };
    }
}