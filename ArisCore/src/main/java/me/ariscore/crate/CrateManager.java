package me.ariscore.crate;

import me.ariscore.ArisCorePlugin;
import me.ariscore.donate.DonateRank;
import me.ariscore.util.ItemUtil;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Donate crates: each crate is a defined type (free, paid-aris, donate); it has rewards
 * with weights. /dc place spawns a crate at the looked-at block + floating hologram.
 * /dc remove deletes the closest crate.
 * Right-click a placed crate to open the GUI of rewards.
 */
public class CrateManager implements Listener {

    private static final String GUI_TITLE = "ArisCrate";

    private final ArisCorePlugin plugin;
    private final File file;
    private final FileConfiguration cfg;

    private final LinkedHashMap<String, CrateType> types = new LinkedHashMap<>();
    private final Map<String, Placement> placements = new HashMap<>(); // worldX_Y_Z -> Placement

    public CrateManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        types.clear(); placements.clear();
        ConfigurationSection typeSec = cfg.getConfigurationSection("types");
        if (typeSec != null) {
            for (String id : typeSec.getKeys(false)) {
                ConfigurationSection sec = typeSec.getConfigurationSection(id);
                if (sec == null) continue;
                Material icon;
                try { icon = Material.valueOf(sec.getString("icon", "CHEST")); } catch (Exception ex) { icon = Material.CHEST; }
                String name = sec.getString("name", id);
                String startHex = sec.getString("start-hex", "FFD700");
                String endHex = sec.getString("end-hex", "FF6A00");
                double price = sec.getDouble("price", 0);
                String donateRequired = sec.getString("donate-required");
                List<CrateReward> rewards = new ArrayList<>();
                List<Map<?, ?>> raw = sec.getMapList("rewards");
                for (Map<?, ?> r : raw) {
                    String material = String.valueOf(r.get("material"));
                    int amount = r.get("amount") instanceof Number n ? n.intValue() : 1;
                    int weight = r.get("weight") instanceof Number w ? w.intValue() : 10;
                    Object labelObj = r.get("name");
                    String label = labelObj != null ? String.valueOf(labelObj) : material;
                    String donate = r.get("donate") == null ? null : String.valueOf(r.get("donate"));
                    Material mat;
                    try { mat = Material.valueOf(material); } catch (Exception ex) { mat = Material.STONE; }
                    rewards.add(new CrateReward(mat, amount, weight, label, donate));
                }
                types.put(id.toLowerCase(), new CrateType(id.toLowerCase(), name, startHex, endHex, icon, price, donateRequired, rewards));
            }
        }
        ConfigurationSection pl = cfg.getConfigurationSection("placements");
        if (pl != null) {
            for (String key : pl.getKeys(false)) {
                ConfigurationSection s = pl.getConfigurationSection(key);
                String type = s.getString("type");
                String world = s.getString("world");
                int x = s.getInt("x"), y = s.getInt("y"), z = s.getInt("z");
                placements.put(key, new Placement(type, world, x, y, z));
            }
        }
    }

    public void save() {
        cfg.set("placements", null);
        for (var e : placements.entrySet()) {
            String base = "placements." + e.getKey() + ".";
            cfg.set(base + "type", e.getValue().type);
            cfg.set(base + "world", e.getValue().world);
            cfg.set(base + "x", e.getValue().x);
            cfg.set(base + "y", e.getValue().y);
            cfg.set(base + "z", e.getValue().z);
        }
        try { cfg.save(file); } catch (IOException ignored) {}
    }

    public List<CrateType> typesList() { return new ArrayList<>(types.values()); }
    public CrateType type(String id) { return types.get(id.toLowerCase()); }

    public boolean place(Player p, String typeId) {
        CrateType type = type(typeId);
        if (type == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Unknown crate type.</red>"))); return false; }
        Block target = p.getTargetBlockExact(8);
        if (target == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Look at a block.</red>"))); return false; }
        Block above = target.getRelative(0, 1, 0);
        above.setType(Material.CHEST);
        String key = key(above.getLocation());
        placements.put(key, new Placement(type.id(), above.getWorld().getName(), above.getX(), above.getY(), above.getZ()));
        spawnHologram(above.getLocation().add(0.5, 1.0, 0.5), type);
        save();
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Crate placed: <b>" + type.displayName() + "</b></grad>")));
        return true;
    }

    public boolean removeAtTarget(Player p) {
        Block target = p.getTargetBlockExact(8);
        if (target == null) return false;
        String key = key(target.getLocation());
        if (!placements.containsKey(key)) return false;
        placements.remove(key);
        target.setType(Material.AIR);
        removeHologramAt(target.getLocation().add(0.5, 1.0, 0.5));
        save();
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FF6A00:#FF0000>Crate removed.</grad>")));
        return true;
    }

    private void spawnHologram(Location loc, CrateType type) {
        ArmorStand st = loc.getWorld().spawn(loc, ArmorStand.class, s -> {
            s.setInvisible(true);
            s.setMarker(true);
            s.setGravity(false);
            s.setSmall(true);
            s.customName(Msg.gradient(type.displayName(), Msg.parseHex(type.startHex(), 0xFFD700), Msg.parseHex(type.endHex(), 0xFF6A00))
                    .decoration(TextDecoration.BOLD, true));
            s.setCustomNameVisible(true);
            s.getPersistentDataContainer().set(plugin.keyCrate(), PersistentDataType.STRING, "1");
        });
    }

    private void removeHologramAt(Location loc) {
        for (var e : loc.getWorld().getNearbyEntities(loc, 0.6, 0.6, 0.6)) {
            if (e.getType() == EntityType.ARMOR_STAND
                    && "1".equals(e.getPersistentDataContainer().get(plugin.keyCrate(), PersistentDataType.STRING))) {
                e.remove();
            }
        }
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Placement pl = placements.get(key(e.getClickedBlock().getLocation()));
        if (pl == null) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        CrateType type = type(pl.type);
        if (type == null) return;

        // Payment / requirements
        if (type.price() > 0) {
            if (!plugin.economy().take(p.getUniqueId(), type.price())) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not enough aris.</red>")));
                return;
            }
        }
        if (type.donateRequired() != null) {
            DonateRank rank = plugin.donates().playerRank(p);
            DonateRank req = plugin.donates().get(type.donateRequired());
            if (rank == null || req == null || rank.weight() < req.weight()) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>Requires donate <b>" + type.donateRequired() + "</b></red>")));
                return;
            }
        }

        // Honour rule: higher donate-tier reward is given over lower if both rolled.
        CrateReward picked = roll(type);
        if (picked.donate() != null) {
            DonateRank cur = plugin.donates().playerRank(p);
            DonateRank rolled = plugin.donates().get(picked.donate());
            if (cur != null && rolled != null && cur.weight() >= rolled.weight()) {
                // already has equal/higher rank -> reroll once non-donate reward
                picked = type.rewards().stream().filter(r -> r.donate() == null).findFirst().orElse(picked);
            } else if (rolled != null) {
                plugin.donates().give(p, rolled.id());
            }
        }
        ItemStack give = new ItemStack(picked.material(), picked.amount());
        p.getInventory().addItem(give).forEach((s, leftover) -> p.getWorld().dropItemNaturally(p.getLocation(), leftover));
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FFD700:#FF6A00>You got <b>" + picked.label() + "</b>!</grad>")));
        Bukkit.broadcast(Msg.prefix().append(Msg.mm("<gray>" + p.getName() + " opened <b>" + type.displayName() + "</b> and won <b>" + picked.label() + "</b>")));
    }

    private CrateReward roll(CrateType type) {
        int total = type.rewards().stream().mapToInt(CrateReward::weight).sum();
        int pick = ThreadLocalRandom.current().nextInt(Math.max(1, total));
        int acc = 0;
        for (CrateReward r : type.rewards()) {
            acc += r.weight();
            if (pick < acc) return r;
        }
        return type.rewards().get(0);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (placements.containsKey(key(e.getBlock().getLocation()))) {
            // protect placement
            if (!e.getPlayer().isOp()) e.setCancelled(true);
        }
    }

    public void openPreview(Player viewer, CrateType type) {
        Inventory inv = Bukkit.createInventory(viewer, 54, Msg.gradient(type.displayName(), Msg.parseHex(type.startHex(), 0xFFD700), Msg.parseHex(type.endHex(), 0xFF6A00))
                .decoration(TextDecoration.BOLD, true));
        List<CrateReward> sorted = new ArrayList<>(type.rewards());
        sorted.sort(Comparator.comparingInt(CrateReward::weight));
        int slot = 0;
        for (CrateReward r : sorted) {
            if (slot >= 54) break;
            ItemStack it = ItemUtil.build(r.material(),
                    "<grad:#" + type.startHex() + ":#" + type.endHex() + ">" + r.label() + "</grad>",
                    List.of(
                            "<gray>Amount: <white>" + r.amount(),
                            "<gray>Weight: <white>" + r.weight(),
                            r.donate() == null ? "<gray>Type: <white>item" : "<gray>Donate: <gold>" + r.donate()
                    ));
            inv.setItem(slot++, it);
        }
        viewer.openInventory(inv);
    }

    private record Placement(String type, String world, int x, int y, int z) {}
}
