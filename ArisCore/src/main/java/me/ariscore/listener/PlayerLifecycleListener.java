package me.ariscore.listener;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/** Teleport on join/death + custom join messages. */
public class PlayerLifecycleListener implements Listener {

    private final ArisCorePlugin plugin;

    public PlayerLifecycleListener(ArisCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.teleport().teleportToSpawn(e.getPlayer());
        e.joinMessage(Msg.prefix().append(Msg.mm("<gray>" + e.getPlayer().getName() + " joined ArisWorld.</gray>")));
        plugin.scoreboard().update(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // PlayerRespawnEvent in TeleportManager handles location; here we override the death-broadcast.
        Component msg = Component.text(e.getEntity().getName() + " died.", NamedTextColor.GRAY);
        e.deathMessage(msg);
    }
}
