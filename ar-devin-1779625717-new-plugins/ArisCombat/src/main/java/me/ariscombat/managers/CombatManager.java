package me.ariscombat.managers;

import me.ariscombat.ArisCombatPlugin;
import me.ariscombat.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит активные КТ-таймеры. Каждые 1 тик пересчитывает action-bar.
 * Сбрасывает флаг по истечении времени.
 *
 * Допущения:
 *   - Игроки с permission {@code ariscombat.bypass} никогда не вступают в КТ.
 *   - Если игрок уже в КТ, повторный {@link #tag(Player)} ОБНОВЛЯЕТ таймер до полного значения.
 */
public class CombatManager {

    private final ArisCombatPlugin plugin;
    private final Map<UUID, Long> expireAt = new HashMap<>();
    /** Сохраняем, был ли флай у игрока ДО входа в КТ — чтобы вернуть его после. */
    private final Map<UUID, Boolean> savedAllowFlight = new HashMap<>();

    public CombatManager(ArisCombatPlugin plugin) {
        this.plugin = plugin;
        startTicker();
    }

    public int durationSeconds() {
        return Math.max(1, plugin.getConfig().getInt("combat.duration-seconds", 20));
    }

    public boolean inCombat(Player player) {
        if (player == null) return false;
        if (player.hasPermission("ariscombat.bypass")) return false;
        Long until = expireAt.get(player.getUniqueId());
        if (until == null) return false;
        if (System.currentTimeMillis() >= until) {
            clear(player);
            return false;
        }
        return true;
    }

    public int secondsLeft(Player player) {
        Long until = expireAt.get(player.getUniqueId());
        if (until == null) return 0;
        long ms = until - System.currentTimeMillis();
        if (ms <= 0) return 0;
        return (int) Math.ceil(ms / 1000.0);
    }

    public void tag(Player player) {
        if (player == null) return;
        if (player.hasPermission("ariscombat.bypass")) return;

        int dur = durationSeconds();
        boolean wasInCombat = expireAt.containsKey(player.getUniqueId());
        expireAt.put(player.getUniqueId(), System.currentTimeMillis() + dur * 1000L);

        // Запоминаем состояние флая один раз — при первом входе в КТ
        if (!wasInCombat) {
            savedAllowFlight.put(player.getUniqueId(), player.getAllowFlight());
            String onTag = plugin.getConfig().getString("combat.on-tag-message",
                    "&c⚔ Вы вступили в бой! Флай и команды отключены на %seconds% сек.");
            player.sendMessage(Msg.parse(onTag.replace("%seconds%", String.valueOf(dur))));
        }
        // Гасим флай
        if (player.isFlying()) player.setFlying(false);
        player.setAllowFlight(false);
    }

    /** Снимает КТ принудительно (например, /ct clear или истечение таймера). */
    public void clear(Player player) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        boolean had = expireAt.remove(id) != null;
        Boolean savedFly = savedAllowFlight.remove(id);
        if (had) {
            if (savedFly != null) player.setAllowFlight(savedFly);
            String onEnd = plugin.getConfig().getString("combat.on-end-message",
                    "&aБой окончен. Можно использовать команды и флай.");
            player.sendMessage(Msg.parse(onEnd));
        }
    }

    /** Освобождает все ресурсы — для onDisable. */
    public void shutdown() {
        expireAt.clear();
        savedAllowFlight.clear();
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (expireAt.isEmpty()) return;
                long now = System.currentTimeMillis();

                // Чистим истекшие
                expireAt.entrySet().removeIf(e -> {
                    if (e.getValue() <= now) {
                        Player p = Bukkit.getPlayer(e.getKey());
                        if (p != null && p.isOnline()) {
                            Boolean savedFly = savedAllowFlight.remove(e.getKey());
                            if (savedFly != null) p.setAllowFlight(savedFly);
                            String onEnd = plugin.getConfig().getString("combat.on-end-message",
                                    "&aБой окончен. Можно использовать команды и флай.");
                            p.sendMessage(Msg.parse(onEnd));
                        } else {
                            savedAllowFlight.remove(e.getKey());
                        }
                        return true;
                    }
                    return false;
                });

                // Action-bar
                if (plugin.getConfig().getBoolean("combat.action-bar", true)) {
                    for (Map.Entry<UUID, Long> e : expireAt.entrySet()) {
                        Player p = Bukkit.getPlayer(e.getKey());
                        if (p == null || !p.isOnline()) continue;
                        int left = (int) Math.ceil((e.getValue() - now) / 1000.0);
                        if (left < 1) left = 1;
                        String text = plugin.getConfig().getString("combat.action-bar-text",
                                "&c⚔ КТ: &e%seconds% сек &7— флай и команды отключены");
                        p.sendActionBar(Msg.parse(text.replace("%seconds%", String.valueOf(left))));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
