package com.iruka.lifelimiter.database;

import com.iruka.lifelimiter.ILifeLimiter;

import java.io.File;

import java.sql.*;
import java.util.UUID;

public class Database {
    private final ILifeLimiter plugin;
    private Connection connection;

    public Database(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File dataFolder = new File(plugin.getDataFolder(), "database.db");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);

            // Создаем таблицу, если её нет
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_hearts (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "hearts INTEGER," +
                        "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка отключения от базы данных: " + e.getMessage());
        }
    }

    public int getPlayerHearts(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT hearts FROM player_hearts WHERE uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("hearts");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка получения сердец игрока: " + e.getMessage());
        }
        return plugin.getConfig().getInt("settings.starting-hearts");
    }

    public void setPlayerHearts(UUID playerUUID, int hearts) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_hearts (uuid, hearts) VALUES (?, ?)")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, hearts);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения сердец игрока: " + e.getMessage());
        }
    }

    public boolean isPlayerBanned(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT hearts FROM player_hearts WHERE uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int hearts = rs.getInt("hearts");
                return hearts == 0;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasPlayerData(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM player_hearts WHERE uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка проверки наличия игрока в базе данных: " + e.getMessage());
            return false;
        }
    }
}