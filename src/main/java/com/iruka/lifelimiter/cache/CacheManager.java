package com.iruka.lifelimiter.cache;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final ILifeLimiter plugin;
    private final Map<UUID, Integer> heartsCache;
    private int taskId;

    public CacheManager(ILifeLimiter plugin) {
        this.plugin = plugin;
        this.heartsCache = new ConcurrentHashMap<>();
    }

    public void startCaching() {
        // Обновляем кэш каждые 30 секунд
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : heartsCache.keySet()) {
                int hearts = plugin.getDatabase().getPlayerHearts(uuid);
                heartsCache.put(uuid, hearts);
            }
        }, 20L * 30, 20L * 30).getTaskId();
    }

    public void stopCaching() {
        Bukkit.getScheduler().cancelTask(taskId);
        saveAll();
    }

    public void setHearts(UUID playerUUID, int hearts) {
        heartsCache.put(playerUUID, hearts);

        // Асинхронно сохраняем в базу данных
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().setPlayerHearts(playerUUID, hearts);
        });
    }

    public int getHearts(UUID playerUUID) {
        int hearts = heartsCache.computeIfAbsent(playerUUID,
                uuid -> plugin.getDatabase().getPlayerHearts(uuid));
        return hearts;
    }

    public void savePlayer(UUID playerUUID) {
        Integer hearts = heartsCache.get(playerUUID);
        if (hearts != null) {
            plugin.getDatabase().setPlayerHearts(playerUUID, hearts);
        }
    }

    public void saveAll() {
        heartsCache.forEach((uuid, hearts) ->
                plugin.getDatabase().setPlayerHearts(uuid, hearts));
    }

    public void invalidateCache(UUID playerUUID) {
        heartsCache.remove(playerUUID);
    }
}