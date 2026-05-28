package me.ariscore.region;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Region (private) management:
 *   - players place a "Region Block" (named iron block) to create a region around them
 *   - default radius from config
 *   - /rg, /rg flag, /rg addowner, /rg addmember, /rg info, /rg rename, /rg list
 *   - 20+ flags (pvp, explode, mobs, redstone, container, item, vehicle, bucket, fire, lava, ice, build, break, sleep, interact, etc.)
 */
public class RegionManager implements Listener {

    public static final List<String> FLAGS = List.of(
            "pvp", "build", "break", "interact",
            "container", "vehicle", "item-drop", "item-pickup",
            "bucket", "fire", "lava", "ice",
            "tnt", "creeper", "mob-griefing", "mob-spawn",
            "sleep", "redstone", "trample", "pistons",
            "frost-walk", "chorus-teleport"
    );

    private final ArisCorePlugin plugin;
    private final File file;
    private final FileConfiguration cfg;
    private final LinkedHashMap<String, Region> regions = new LinkedHashMap<>();

    public RegionManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "regions.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        regions.clear();
        ConfigurationSection root = cfg.getConfigurationSection("regions");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            UUID owner = UUID.fromString(s.getString("owner"));
            Location loc = new Location(
                    Bukkit.getWorld(s.getString("world", "world")),
                    s.getDouble("x"), s.getDouble("y"), s.getDouble("z"));
            Region r = new Region(s.getString("name", key), owner, loc, s.getInt("radius", 20));
            for (String m : s.getStringList("members")) r.members().add(UUID.fromString(m));
            ConfigurationSection flagsSec = s.getConfigurationSection("flags");
            if (flagsSec != null) for (String f : flagsSec.getKeys(false)) r.setFlag(f, flagsSec.getBoolean(f));
            regions.put(key, r);
        }
    }

    public void save() {
        cfg.set("regions", null);
        for (var e : regions.entrySet()) {
            String base = "regions." + e.getKey() + ".";
            Region r = e.getValue();
            cfg.set(base + "name", r.name());
            cfg.set(base + "owner", r.owner().toString());
            cfg.set(base + "world", r.world());
            cfg.set(base + "x", r.x());
            cfg.set(base + "y", r.y());
            cfg.set(base + "z", r.z());
            cfg.set(base + "radius", r.radius());
            List<String> members = new ArrayList<>();
            for (UUID m : r.members()) members.add(m.toString());
            cfg.set(base + "members", members);
            for (var fe : r.flags().entrySet()) cfg.set(base + "flags." + fe.getKey(), fe.getValue());
        }
        try { cfg.save(file); } catch (IOException e) { plugin.getLogger().warning("regions save: " + e); }
    }

    public Region regionAt(Location loc) {
        for (Region r : regions.values()) if (r.contains(loc)) return r;
        return null;
    }

    public List<Region> ownedBy(UUID id) {
        List<Region> out = new ArrayList<>();
        for (Region r : regions.values()) if (r.owner().equals(id)) out.add(r);
        return out;
    }

    public Region byName(String n) {
        for (Region r : regions.values()) if (r.name().equalsIgnoreCase(n)) return r;
        return null;
    }

    public boolean canBuild(Player p, Location loc) {
        Region r = regionAt(loc);
        if (r == null) return true;
        return r.isAllowed(p.getUniqueId());
    }

    public ItemStack regionBlockItem() {
        ItemStack it = new ItemStack(Material.IRON_BLOCK);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Msg.mm("<grad:#FFD700:#FF6A00><b>Region Block</b></grad>"));
        meta.lore(Msg.mmList(List.of(
                "<gray>Place to create a private region.",
                "<gray>Default radius: <yellow>" + plugin.getConfig().getInt("regions.default-radius", 20),
                "<gray>Right-click your region block to manage."
        )));
        meta.getPersistentDataContainer().set(plugin.keyRegionBlock(), PersistentDataType.STRING, "1");
        it.setItemMeta(meta);
        return it;
    }

    public boolean isRegionBlock(ItemStack it) {
        if (it == null || it.getItemMeta() == null) return false;
        return "1".equals(it.getItemMeta().getPersistentDataContainer().get(plugin.keyRegionBlock(), PersistentDataType.STRING));
    }

    public Region create(Player owner, Location centre) {
        int radius = plugin.getConfig().getInt("regions.default-radius", 20);
        String name = owner.getName() + "-" + (regions.size() + 1);
        String key = "rg" + System.currentTimeMillis();
        Region r = new Region(name, owner.getUniqueId(), centre, radius);
        regions.put(key, r);
        save();
        return r;
    }

    public boolean delete(Region r) {
        Iterator<java.util.Map.Entry<String, Region>> it = regions.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, Region> e = it.next();
            if (e.getValue() == r) { it.remove(); save(); return true; }
        }
        return false;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack it = e.getItemInHand();
        if (!isRegionBlock(it)) {
            if (!canBuild(e.getPlayer(), e.getBlock().getLocation())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Msg.prefix().append(Msg.mm("<red>This area is protected.</red>")));
            }
            return;
        }
        Location loc = e.getBlock().getLocation();
        Region existing = regionAt(loc);
        if (existing != null) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Msg.prefix().append(Msg.mm("<red>Already inside a region.</red>")));
            return;
        }
        Region r = create(e.getPlayer(), loc);
        e.getPlayer().sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Region <b>" + r.name() + "</b> created (radius " + r.radius() + ").</grad>")));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!canBuild(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Msg.prefix().append(Msg.mm("<red>This area is protected.</red>")));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (!canBuild(e.getPlayer(), e.getClickedBlock().getLocation())) {
            Region r = regionAt(e.getClickedBlock().getLocation());
            if (r != null && !r.flag("interact", true)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> {
            Region r = regionAt(b.getLocation());
            return r != null && !r.flag("tnt", false);
        });
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        Region r = regionAt(e.getLocation());
        if (r == null) return;
        if (!r.flag("mob-spawn", true) && e.getEntity() instanceof org.bukkit.entity.Monster) e.setCancelled(true);
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim) || !(e.getDamager() instanceof Player attacker)) return;
        Region r = regionAt(victim.getLocation());
        if (r != null && !r.flag("pvp", false)) {
            e.setCancelled(true);
            attacker.sendMessage(Msg.prefix().append(Msg.mm("<red>PVP is disabled here.</red>")));
        }
    }
}
