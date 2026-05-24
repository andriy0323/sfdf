package me.arisguard.listeners;

import me.arisguard.ArisGuardPlugin;
import me.arisguard.managers.Msg;
import me.arisguard.managers.OpGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Locale;

/**
 * Перехватывает /op от игроков и проверяет OP при заходе.
 *
 *   - PlayerCommandPreprocessEvent: если intercept-op-command=true и
 *     отправитель — игрок, не входящий в allowed-ops, команда отменяется.
 *   - PlayerJoinEvent: если игрок вошёл с флагом OP, но его нет в
 *     allowed-ops — мгновенно снимается OP и пишется в лог.
 */
public class OpListener implements Listener {

    private final ArisGuardPlugin plugin;

    public OpListener(ArisGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        OpGuard og = plugin.getOpGuard();
        if (!og.isEnabled() || !og.isInterceptOpCmd()) return;

        String msg = e.getMessage();
        if (msg == null || msg.length() < 2) return;

        String cmd = msg.substring(1).split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (!(cmd.equals("op") || cmd.equals("minecraft:op")
                || cmd.equals("deop") || cmd.equals("minecraft:deop"))) return;

        Player p = e.getPlayer();
        if (!og.isAllowed(p.getName())) {
            e.setCancelled(true);
            p.sendMessage(Msg.parse("&c[ArisGuard] Команда /op запрещена. Только администратор сервера."));
            plugin.getLogger().warning("[ArisGuard] OP-Guard: игрок " + p.getName()
                    + " попытался выполнить " + msg + " — отменено.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        OpGuard og = plugin.getOpGuard();
        if (!og.isEnabled()) return;
        Player p = e.getPlayer();
        if (p.isOp() && !og.isAllowed(p.getName())) {
            p.setOp(false);
            plugin.getLogger().warning("[ArisGuard] OP-Guard: " + p.getName()
                    + " вошёл с OP, но не в allowed-ops — снят.");
        }
    }
}
