package me.ariscore.region;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Cuboid region defined by a centre block + radius. */
public final class Region {

    private String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final String world;
    private final int x, y, z;
    private final int radius;
    private final Map<String, Boolean> flags = new HashMap<>();

    public Region(String name, UUID owner, Location centre, int radius) {
        this.name = name;
        this.owner = owner;
        this.world = centre.getWorld().getName();
        this.x = centre.getBlockX();
        this.y = centre.getBlockY();
        this.z = centre.getBlockZ();
        this.radius = radius;
    }

    public String name()         { return name; }
    public UUID owner()          { return owner; }
    public Set<UUID> members()   { return members; }
    public String world()        { return world; }
    public int x()               { return x; }
    public int y()               { return y; }
    public int z()               { return z; }
    public int radius()          { return radius; }
    public Map<String, Boolean> flags() { return flags; }

    public void setName(String n) { this.name = n; }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(world)) return false;
        return Math.abs(loc.getBlockX() - x) <= radius
                && Math.abs(loc.getBlockZ() - z) <= radius
                && Math.abs(loc.getBlockY() - y) <= radius;
    }

    public boolean isAllowed(UUID id) {
        return owner.equals(id) || members.contains(id);
    }

    public boolean flag(String name, boolean defaultValue) {
        return flags.getOrDefault(name.toLowerCase(), defaultValue);
    }

    public void setFlag(String name, boolean value) {
        flags.put(name.toLowerCase(), value);
    }
}
