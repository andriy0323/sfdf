package me.ariscombat.listeners;

import me.ariscombat.ArisCombatPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Помечает обоих игроков КТ при PvP. Стрелы и снаряды учитываются по shooter.
 */
public class CombatTagListener implements Listener {

    private final ArisCombatPlugin plugin;

    public CombatTagListener(ArisCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getFinalDamage() <= 0) return;

        Player victim   = playerOf(e.getEntity());
        Player attacker = attackerPlayer(e.getDamager());

        if (victim == null || attacker == null) return;
        if (victim.equals(attacker)) return;

        plugin.getCombatManager().tag(victim);
        plugin.getCombatManager().tag(attacker);
    }

    private static Player playerOf(Entity e) { return e instanceof Player p ? p : null; }

    private static Player attackerPlayer(Entity dam) {
        if (dam instanceof Player p) return p;
        if (dam instanceof Projectile pr && pr.getShooter() instanceof Player p) return p;
        return null;
    }
}
