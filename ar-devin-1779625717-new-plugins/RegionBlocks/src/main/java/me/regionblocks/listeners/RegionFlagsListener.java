package me.regionblocks.listeners;

import me.regionblocks.RegionBlocks;
import me.regionblocks.models.Region;
import me.regionblocks.models.RegionFlags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Применение настраиваемых флагов региона:
 *   pvp         → запрет/разрешение PvP между игроками
 *   mob-spawn   → запрет спавна враждебных мобов
 *   mob-damage  → запрет урона от мобов игрокам внутри
 *   explosions  → запрет урона от взрывов сущностей (TNT/криперы/etc.)
 *   fire-spread → запрет распространения огня (не игроком)
 *
 * Защита от поломки/постройки блоков и т.п. остаётся в ProtectionListener.
 */
public class RegionFlagsListener implements Listener {

    private final RegionBlocks plugin;

    public RegionFlagsListener(RegionBlocks plugin) {
        this.plugin = plugin;
    }

    // ── PvP ───────────────────────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        Player victim = playerOf(e.getEntity());
        Player attacker = attackerPlayer(e.getDamager());
        if (victim == null || attacker == null) return;
        if (victim.equals(attacker)) return;

        Region atVictim = plugin.getRegionManager().getRegionAt(victim.getLocation());
        Region atAttacker = plugin.getRegionManager().getRegionAt(attacker.getLocation());
        Region region = atVictim != null ? atVictim : atAttacker;
        if (region == null) return;

        if (attacker.hasPermission("regionblocks.admin")) return;

        if (!RegionFlags.valueFor(region, RegionFlags.PVP)) {
            e.setCancelled(true);
            attacker.sendMessage(Component.text("✗ PvP запрещено в этом регионе.")
                    .color(TextColor.color(0xFF4444)));
        }
    }

    // ── Урон от мобов ─────────────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobDamagePlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Entity dam = e.getDamager();
        Entity src = (dam instanceof Projectile pr && pr.getShooter() instanceof Entity ent) ? ent : dam;
        if (src instanceof Player) return;
        if (!(src instanceof LivingEntity)) return;

        Region region = plugin.getRegionManager().getRegionAt(victim.getLocation());
        if (region == null) return;

        if (!RegionFlags.valueFor(region, RegionFlags.MOB_DAMAGE)) {
            e.setCancelled(true);
        }
    }

    // ── Спавн враждебных мобов ────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof Monster)) return;
        Region region = plugin.getRegionManager().getRegionAt(e.getLocation());
        if (region == null) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM
                || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND) return;
        if (!RegionFlags.valueFor(region, RegionFlags.MOB_SPAWN)) {
            e.setCancelled(true);
        }
    }

    // ── Взрывы (урон / разрушение) ───────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        Location at = e.getLocation();
        Region region = plugin.getRegionManager().getRegionAt(at);
        if (region == null) return;
        // Кастомные TNT уже обрабатываются TntExplosionListener (контроль blockList).
        if (e.getEntity() instanceof TNTPrimed) return;
        if (!RegionFlags.valueFor(region, RegionFlags.EXPLOSIONS)) {
            e.blockList().clear();
            e.setYield(0f);
        }
    }

    // ── Распространение огня ─────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireSpread(BlockIgniteEvent e) {
        if (e.getPlayer() != null) return; // ProtectionListener сам обрабатывает игроков
        Region region = plugin.getRegionManager().getRegionAt(e.getBlock().getLocation());
        if (region == null) return;
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD
                || e.getCause() == BlockIgniteEvent.IgniteCause.LAVA
                || e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            if (!RegionFlags.valueFor(region, RegionFlags.FIRE_SPREAD)) {
                e.setCancelled(true);
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private Player playerOf(Entity e) {
        return e instanceof Player p ? p : null;
    }

    private Player attackerPlayer(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile pr && pr.getShooter() instanceof Player p) return p;
        return null;
    }
}
