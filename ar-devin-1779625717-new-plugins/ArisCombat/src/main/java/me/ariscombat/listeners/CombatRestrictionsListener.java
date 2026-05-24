package me.ariscombat.listeners;

import me.ariscombat.ArisCombatPlugin;
import me.ariscombat.util.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.List;
import java.util.Locale;

/**
 * Реализует ограничения во время КТ:
 *  - блок команд (whitelist в config)
 *  - блок включения флая
 */
public class CombatRestrictionsListener implements Listener {

    private final ArisCombatPlugin plugin;

    public CombatRestrictionsListener(ArisCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getCombatManager().inCombat(p)) return;

        String raw = e.getMessage();
        if (raw.startsWith("/")) raw = raw.substring(1);
        String head = raw.split(" ", 2)[0].toLowerCase(Locale.ROOT);
        if (head.contains(":")) head = head.substring(head.indexOf(':') + 1);

        List<String> allowed = plugin.getConfig().getStringList("combat.allowed-commands");
        for (String a : allowed) {
            if (head.equalsIgnoreCase(a)) return;
        }

        e.setCancelled(true);
        int left = plugin.getCombatManager().secondsLeft(p);
        String msg = plugin.getConfig().getString("combat.on-blocked-command",
                "&c⚔ Эту команду нельзя использовать во время КТ. Осталось: &e%seconds% сек");
        p.sendMessage(Msg.parse(msg.replace("%seconds%", String.valueOf(left))));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getCombatManager().inCombat(p)) return;
        if (!e.isFlying()) return; // выключение разрешено

        e.setCancelled(true);
        p.setAllowFlight(false);
        int left = plugin.getCombatManager().secondsLeft(p);
        String msg = plugin.getConfig().getString("combat.on-blocked-fly",
                "&c⚔ Флай отключён во время КТ. Осталось: &e%seconds% сек");
        p.sendMessage(Msg.parse(msg.replace("%seconds%", String.valueOf(left))));
    }
}
