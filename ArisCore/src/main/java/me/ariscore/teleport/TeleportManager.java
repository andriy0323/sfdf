package me.ariscore.teleport;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager implements Listener {

    private final ArisCorePlugin plugin;
    private final File spawnsFile;
    private final FileConfiguration spawns;
    private final File warpsFile;
    private final FileConfiguration warps;

    private final Map<UUID, UUID> pendingTpa = new HashMap<>(); // target -> requester
    private final Map<UUID, Long> tpaExpires = new HashMap<>();

    public TeleportManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        this.spawnsFile = new File(plugin.getDataFolder(), "spawn.yml");
        this.spawns = YamlConfiguration.loadConfiguration(spawnsFile);
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.warps = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void setSpawn(Location loc) {
        spawns.set("spawn.world", loc.getWorld().getName());
        spawns.set("spawn.x", loc.getX());
        spawns.set("spawn.y", loc.getY());
        spawns.set("spawn.z", loc.getZ());
        spawns.set("spawn.yaw", loc.getYaw());
        spawns.set("spawn.pitch", loc.getPitch());
        try { spawns.save(spawnsFile); } catch (IOException ignored) {}
    }

    public Location getSpawn() {
        if (!spawns.contains("spawn.world")) return null;
        World w = Bukkit.getWorld(spawns.getString("spawn.world", "world"));
        if (w == null) return null;
        return new Location(w,
                spawns.getDouble("spawn.x"),
                spawns.getDouble("spawn.y"),
                spawns.getDouble("spawn.z"),
                (float) spawns.getDouble("spawn.yaw"),
                (float) spawns.getDouble("spawn.pitch"));
    }

    public void teleportToSpawn(Player p) {
        Location l = getSpawn();
        if (l == null) {
            World w = Bukkit.getWorlds().get(0);
            l = w.getSpawnLocation();
        }
        p.teleport(l);
    }

    public boolean setWarp(String name, Location loc) {
        warps.set("warps." + name + ".world", loc.getWorld().getName());
        warps.set("warps." + name + ".x", loc.getX());
        warps.set("warps." + name + ".y", loc.getY());
        warps.set("warps." + name + ".z", loc.getZ());
        warps.set("warps." + name + ".yaw", loc.getYaw());
        warps.set("warps." + name + ".pitch", loc.getPitch());
        try { warps.save(warpsFile); return true; } catch (IOException ignored) { return false; }
    }

    public Location getWarp(String name) {
        String base = "warps." + name + ".";
        if (!warps.contains(base + "world")) return null;
        World w = Bukkit.getWorld(warps.getString(base + "world"));
        if (w == null) return null;
        return new Location(w,
                warps.getDouble(base + "x"),
                warps.getDouble(base + "y"),
                warps.getDouble(base + "z"),
                (float) warps.getDouble(base + "yaw"),
                (float) warps.getDouble(base + "pitch"));
    }

    public boolean delWarp(String name) {
        if (!warps.contains("warps." + name)) return false;
        warps.set("warps." + name, null);
        try { warps.save(warpsFile); } catch (IOException ignored) {}
        return true;
    }

    public java.util.Set<String> warpNames() {
        return warps.getConfigurationSection("warps") == null
                ? java.util.Collections.emptySet()
                : warps.getConfigurationSection("warps").getKeys(false);
    }

    public void sendTpa(Player from, Player to) {
        pendingTpa.put(to.getUniqueId(), from.getUniqueId());
        tpaExpires.put(to.getUniqueId(), System.currentTimeMillis() + 60_000L);

        from.sendMessage(Msg.prefix().append(Msg.mm("<gray>TPA request sent to <yellow>" + to.getName() + "</yellow>. They have 60s to respond.")));
        Component line = Msg.prefix()
                .append(Msg.mm("<gray>Player <yellow>" + from.getName() + "</yellow> wants to teleport to you. "))
                .append(Msg.mm("<click:run:/tpaccept><grad:#7CFC00:#32CD32><b>[ACCEPT]</b></grad></click> "))
                .append(Msg.mm("<click:run:/tpadeny><grad:#FF6A00:#FF0000><b>[DENY]</b></grad></click>"));
        to.sendMessage(line);
    }

    public boolean acceptTpa(Player target) {
        UUID requester = pendingTpa.remove(target.getUniqueId());
        Long exp = tpaExpires.remove(target.getUniqueId());
        if (requester == null || exp == null || exp < System.currentTimeMillis()) {
            target.sendMessage(Msg.prefix().append(Msg.mm("<red>No pending request.</red>")));
            return false;
        }
        Player req = Bukkit.getPlayer(requester);
        if (req == null) {
            target.sendMessage(Msg.prefix().append(Msg.mm("<red>Requester is offline.</red>")));
            return false;
        }
        req.teleport(target.getLocation());
        target.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Accepted.</grad>")));
        req.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>" + target.getName() + " accepted your TPA.</grad>")));
        return true;
    }

    public boolean denyTpa(Player target) {
        UUID requester = pendingTpa.remove(target.getUniqueId());
        tpaExpires.remove(target.getUniqueId());
        if (requester == null) return false;
        Player req = Bukkit.getPlayer(requester);
        target.sendMessage(Msg.prefix().append(Msg.mm("<gray>Request denied.</gray>")));
        if (req != null) req.sendMessage(Msg.prefix().append(Msg.mm("<red>" + target.getName() + " denied your TPA.</red>")));
        return true;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Location spawn = getSpawn();
        if (spawn != null) e.setRespawnLocation(spawn);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // teleport on death handled by respawn event above
    }
}
