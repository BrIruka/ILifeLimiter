package com.iruka.lifelimiter.items;

import com.iruka.lifelimiter.ILifeLimiter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemManager {
    private final ILifeLimiter plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey uniqueKey;

    public ItemManager(ILifeLimiter plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "life-stealer");
        this.uniqueKey = new NamespacedKey(plugin, "unique-id");
    }

    public ItemStack createLifeStealer() {
        String materialName = plugin.getConfig().getString("items.life-stealer.material", "STICK");
        Material material = Material.valueOf(materialName.toUpperCase());
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Установка имени
            String name = plugin.getConfig().getString("items.life-stealer.name", "&c&lШприц жизни");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            // Установка описания
            List<String> lore = plugin.getConfig().getStringList("items.life-stealer.lore");
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            // Добавление свечения если включено
            if (plugin.getConfig().getBoolean("items.life-stealer.glow", true)) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Добавление уникальных идентификаторов
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, "life-stealer");
            meta.getPersistentDataContainer().set(uniqueKey, PersistentDataType.STRING, UUID.randomUUID().toString());

            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean isLifeStealer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null &&
                meta.getPersistentDataContainer().has(itemKey, PersistentDataType.STRING) &&
                "life-stealer".equals(meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING));
    }
}