package com.iruka.lifelimiter.listeners;

import com.iruka.lifelimiter.ILifeLimiter;
import com.iruka.lifelimiter.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LifeStealerListener implements Listener {
    private final ILifeLimiter plugin;
    private final Set<UUID> cooldown = new HashSet<>();

    public LifeStealerListener(ILifeLimiter plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack pickedItem = event.getItem().getItemStack();
        if (!plugin.getItemManager().isLifeStealer(pickedItem)) return;

        // Проверяем инвентарь на наличие свободного слота
        if (player.getInventory().firstEmpty() == -1) {
            event.setCancelled(true);
            return;
        }

        // Отменяем стандартное поведение и добавляем предмет в первый свободный слот
        event.setCancelled(true);
        player.getInventory().addItem(pickedItem);
        event.getItem().remove();
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;

        Player player = event.getPlayer();

        // Проверка кулдауна
        if (cooldown.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getItemManager().isLifeStealer(item)) return;

        // Устанавливаем кулдаун
        cooldown.add(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> cooldown.remove(player.getUniqueId()), 6L);

        // Отменяем событие сразу
        event.setCancelled(true);

        // Проверка на самого себя
        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.cannot-steal-own-heart")));
            return;
        }

        // Проверка количества сердец
        int targetHearts = plugin.getPlayerHearts(target.getUniqueId());
        int playerHearts = plugin.getPlayerHearts(player.getUniqueId());
        int minHearts = plugin.getConfig().getInt("settings.min-hearts");
        int maxHearts = plugin.getConfig().getInt("settings.max-hearts");

        // Проверка минимального количества сердец у цели
        if (targetHearts <= minHearts) {
            player.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.cannot-steal-last-heart")));
            return;
        }

        // Проверка максимального количества сердец у игрока
        if (playerHearts >= maxHearts) {
            player.sendMessage(ColorUtils.colorize(
                    plugin.getLanguageManager().getMessage("messages.max-hearts-exceeded")
                            .replace("%max%", String.valueOf(maxHearts))));
            return;
        }

        // Удаляем предмет до выполнения действия
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        // Кража жизни
        plugin.setPlayerHearts(target.getUniqueId(), targetHearts - 1);
        plugin.setPlayerHearts(player.getUniqueId(), playerHearts + 1);

        // Обновление здоровья
        plugin.updatePlayerHealth(target);
        plugin.updatePlayerHealth(player);

        // Эффекты
        DustOptions dustOptions = new DustOptions(Color.RED, 1.0f);
        target.getWorld().spawnParticle(Particle.REDSTONE,
                target.getLocation().add(0, 1, 0),
                50, 0.5, 0.5, 0.5, 0,
                dustOptions);

        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // Сообщения
        target.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("messages.life-stolen-from")
                        .replace("%hearts%", String.valueOf(targetHearts - 1))));

        player.sendMessage(ColorUtils.colorize(
                plugin.getLanguageManager().getMessage("messages.life-stolen-success")
                        .replace("%player%", target.getName())
                        .replace("%hearts%", String.valueOf(playerHearts + 1))));
    }
}