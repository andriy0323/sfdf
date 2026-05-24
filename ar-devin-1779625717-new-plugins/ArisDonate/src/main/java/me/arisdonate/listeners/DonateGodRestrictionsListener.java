package me.arisdonate.listeners;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.commands.GodCommand;
import me.arisdonate.models.StaffRank;
import me.arisdonate.util.Msg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/**
 * Ограничения для божественного режима у донатеров:
 *  - не могут подбирать ресурсы (PlayerAttemptPickupItemEvent)
 *  - не могут бить других игроков (EntityDamageByEntityEvent)
 *
 * Стаф-админы (вес >= 170) — без ограничений, т.е. полный god.
 *
 * Проверка идёт через {@link GodCommand#isGod(java.util.UUID)} — этим
 * флагом отмечается активный режим у конкретного игрока, не сама перма.
 */
public class DonateGodRestrictionsListener implements Listener {

    private static final int STAFF_ADMIN_WEIGHT = 170;

    private final ArisDonatePlugin plugin;

    public DonateGodRestrictionsListener(ArisDonatePlugin plugin) {
        this.plugin = plugin;
    }

    /** true, если у игрока стаф-ранг Admin+ — для них god без ограничений. */
    private boolean isStaffAdmin(Player p) {
        try {
            StaffRank sr = plugin.getStaffManager().getPlayerRank(p.getName());
            return sr != null && sr.weight() >= STAFF_ADMIN_WEIGHT;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean restricted(Player p) {
        if (p == null) return false;
        if (!GodCommand.isGod(p.getUniqueId())) return false;
        return !isStaffAdmin(p);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        Player p = e.getPlayer();
        if (!restricted(p)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        Player attacker = attackerPlayer(e.getDamager());
        if (attacker == null) return;
        if (!(e.getEntity() instanceof Player victim)) return;
        if (attacker.equals(victim)) return;
        if (!restricted(attacker)) return;

        e.setCancelled(true);
        attacker.sendMessage(Msg.parse("&c⚔ В режиме god нельзя бить других игроков. Сначала &e/god&c."));
    }

    private static Player attackerPlayer(Entity dam) {
        if (dam instanceof Player p) return p;
        if (dam instanceof Projectile pr && pr.getShooter() instanceof Player p) return p;
        return null;
    }
}
