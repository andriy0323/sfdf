package me.ariscore.storage;

import me.ariscore.ArisCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks kills, deaths and playtime (online seconds) per player. */
public class StatsManager implements Listener {

    private final ArisCorePlugin plugin;
    private final Map<UUID, Long> joinTimes = new HashMap<>();

    public StatsManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        for (Player p : Bukkit.getOnlinePlayers()) joinTimes.put(p.getUniqueId(), System.currentTimeMillis());
        Bukkit.getScheduler().runTaskTimer(plugin, this::flushAll, 20L * 60L, 20L * 60L);
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        joinTimes.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        flushOne(e.getPlayer().getUniqueId());
    }

    @EventHandler public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        FileConfiguration v = plugin.data().load(victim);
        v.set("stats.deaths", v.getInt("stats.deaths") + 1);
        plugin.data().save(victim, v);
        Player killer = victim.getKiller();
        if (killer != null && !killer.equals(victim)) {
            FileConfiguration k = plugin.data().load(killer);
            k.set("stats.kills", k.getInt("stats.kills") + 1);
            plugin.data().save(killer, k);
        }
    }

    private void flushAll() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> e : joinTimes.entrySet()) {
            addPlaytimeSeconds(e.getKey(), (now - e.getValue()) / 1000L);
            e.setValue(now);
        }
    }

    private void flushOne(UUID id) {
        Long t = joinTimes.remove(id);
        if (t == null) return;
        addPlaytimeSeconds(id, (System.currentTimeMillis() - t) / 1000L);
    }

    private void addPlaytimeSeconds(UUID id, long add) {
        if (add <= 0) return;
        FileConfiguration cfg = plugin.data().load(id);
        cfg.set("stats.playtime", cfg.getLong("stats.playtime") + add);
        plugin.data().save(id, cfg);
    }

    public int kills(Player p)     { return plugin.data().load(p).getInt("stats.kills"); }
    public int deaths(Player p)    { return plugin.data().load(p).getInt("stats.deaths"); }
    public long playtime(Player p) {
        long stored = plugin.data().load(p).getLong("stats.playtime");
        Long jt = joinTimes.get(p.getUniqueId());
        if (jt != null) stored += (System.currentTimeMillis() - jt) / 1000L;
        return stored;
    }
}
