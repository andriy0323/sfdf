package me.arisguard.listeners;

import me.arisguard.ArisGuardPlugin;
import me.arisguard.managers.ConnectionGuard;
import me.arisguard.managers.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Применяет {@link ConnectionGuard} к асинхронным pre-login событиям и
 * считает онлайн-подключения по IP.
 */
public class PreLoginListener implements Listener {

    private final ArisGuardPlugin plugin;

    public PreLoginListener(ArisGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        ConnectionGuard cg = plugin.getConnectionGuard();
        if (!cg.isEnabled()) return;
        String ip = e.getAddress() == null ? null : e.getAddress().getHostAddress();
        String reason = cg.evaluate(ip);
        if (reason != null) {
            Component msg = Msg.parse(reason);
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        plugin.getConnectionGuard().onJoin(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        plugin.getConnectionGuard().onQuit(e.getPlayer());
    }
}
