package com.iruka.lifelimiter.utils;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {
    private final ILifeLimiter plugin;
    private final Map<String, YamlConfiguration> languages;
    private YamlConfiguration currentLanguage;
    private String currentLangCode;

    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>() {{
        put("en", "English");
        put("ru", "Русский");
        put("uk", "Українська");
        put("pl", "Polski");
        put("zh", "中文");
        put("fr", "Français");
        put("es", "Español");
    }};

    public LanguageManager(ILifeLimiter plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        loadLanguages();
    }

    private void loadLanguages() {
        // Создаем директорию lang, если её нет
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Копируем файлы языков из ресурсов
        for (String langCode : SUPPORTED_LANGUAGES.keySet()) {
            File langFile = new File(langDir, langCode + ".yml");
            if (!langFile.exists()) {
                try (InputStream in = plugin.getResource("lang/" + langCode + ".yml")) {
                    if (in != null) {
                        Files.copy(in, langFile.toPath());
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Не удалось создать файл языка: " + langCode, e);
                }
            }

            // Загружаем конфигурацию языка
            YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            languages.put(langCode, langConfig);
        }

        // Устанавливаем текущий язык из конфига
        setLanguage(plugin.getConfig().getString("settings.language", "en"));
    }

    public void setLanguage(String langCode) {
        if (!SUPPORTED_LANGUAGES.containsKey(langCode)) {
            plugin.getLogger().warning("Неподдерживаемый язык: " + langCode + ". Используется английский язык.");
            langCode = "en";
        }

        currentLangCode = langCode;
        currentLanguage = languages.get(langCode);
        plugin.getConfig().set("settings.language", langCode);
        plugin.saveConfig();
    }

    public String getMessage(String path) {
        String message = currentLanguage.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Отсутствует перевод для: " + path + " в языке: " + currentLangCode);
            // Пытаемся получить сообщение из английского языка как запасной вариант
            message = languages.get("en").getString(path, "Missing translation: " + path);
        }
        return message;
    }

    public String getCurrentLanguage() {
        return currentLangCode;
    }

    public Map<String, String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    public void reloadLanguages() {
        languages.clear();
        loadLanguages();
    }
}