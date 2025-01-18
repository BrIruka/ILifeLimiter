package com.iruka.lifelimiter;

import com.iruka.lifelimiter.cache.CacheManager;
import com.iruka.lifelimiter.commands.LivesCommandExecutor;
import com.iruka.lifelimiter.commands.LivesTabCompleter;
import com.iruka.lifelimiter.database.Database;
import com.iruka.lifelimiter.handlers.BanHandler;
import com.iruka.lifelimiter.items.ItemManager;
import com.iruka.lifelimiter.listeners.LifeStealerListener;
import com.iruka.lifelimiter.listeners.PlayerDeathListener;
import com.iruka.lifelimiter.listeners.PlayerHealthManager;
import com.iruka.lifelimiter.listeners.PlayerQuitListener;
import com.iruka.lifelimiter.placeholders.LivesExpansion;
import com.iruka.lifelimiter.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ILifeLimiter extends JavaPlugin {
    private Map<UUID, Integer> playerHearts = new HashMap<>();
    private FileConfiguration config;
    private Database database;
    private CacheManager cacheManager;
    private PlayerHealthManager healthManager;
    private BanHandler banHandler;
    private LanguageManager languageManager;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();

        languageManager = new LanguageManager(this);

        database = new Database(this);
        database.connect();
        cacheManager = new CacheManager(this);
        cacheManager.startCaching();


        getCommand("ll").setExecutor(new LivesCommandExecutor(this));
        getCommand("ll").setTabCompleter(new LivesTabCompleter());

        itemManager = new ItemManager(this);
        getServer().getPluginManager().registerEvents(new LifeStealerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);


        healthManager = new PlayerHealthManager(this);
        getServer().getPluginManager().registerEvents(healthManager, this);

        banHandler = new BanHandler(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LivesExpansion(this).register();
            getLogger().info("PlaceholderAPI найден, плейсхолдеры зарегистрированы!");
        }
    }

    @Override
    public void onDisable() {
        if (cacheManager != null) {
            cacheManager.stopCaching();
        }
        if (database != null) {
            database.disconnect();
        }
    }

    public Database getDatabase() {
        return database;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public int getPlayerHearts(UUID playerUUID) {
        return cacheManager.getHearts(playerUUID);
    }

    public BanHandler getBanHandler() {
        return banHandler;
    }

    public void updatePlayerHealth(Player player) {
        healthManager.updatePlayerHealth(player);
    }

    public void removeHeart(UUID playerUUID) {
        int currentHearts = getPlayerHearts(playerUUID);
        int newHearts = Math.max(0, currentHearts - 1);

        setPlayerHearts(playerUUID, newHearts);
    }

    public void setPlayerHearts(UUID playerUUID, int hearts) {
        cacheManager.setHearts(playerUUID, hearts);
    }

    public LanguageManager getLanguageManager(){
        return languageManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        config = getConfig();
        if (languageManager != null) {
            languageManager.reloadLanguages();
        }
    }
}