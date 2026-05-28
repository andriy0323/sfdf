package me.ariscore.donate;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.ItemUtil;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Loads the 20+ donate / staff ranks from donates.yml, persists per-player rank
 * via PlayerDataManager, manages the /donate /don shulker GUI, the /donate
 * admin tools and the per-player command/permission attachments.
 */
public class DonateManager implements Listener {

    private static final String GUI_TITLE = "ArisDonate";

    private final ArisCorePlugin plugin;
    private final LinkedHashMap<String, DonateRank> ranks = new LinkedHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public DonateManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        reload();
        for (Player p : Bukkit.getOnlinePlayers()) applyAttachment(p);
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "donates.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ranks.clear();
        ConfigurationSection root = cfg.getConfigurationSection("ranks");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection rs = root.getConfigurationSection(id);
            if (rs == null) continue;
            ranks.put(id.toLowerCase(), new DonateRank(
                    id.toLowerCase(),
                    rs.getString("name", id),
                    rs.getString("start-hex", "FFFFFF"),
                    rs.getString("end-hex", "FFFFFF"),
                    rs.getInt("weight", 1),
                    rs.getInt("region-limit", 1),
                    rs.getInt("home-limit", 1),
                    parseMat(rs.getString("shulker", "WHITE_SHULKER_BOX")),
                    rs.getStringList("description"),
                    rs.getStringList("permissions"),
                    rs.getStringList("commands"),
                    rs.getInt("gui-slot", 0),
                    rs.getString("kit-id"),
                    rs.getBoolean("staff", false)
            ));
        }
        for (Player p : Bukkit.getOnlinePlayers()) applyAttachment(p);
    }

    private Material parseMat(String s) {
        try { return Material.valueOf(s); } catch (Exception e) { return Material.WHITE_SHULKER_BOX; }
    }

    public DonateRank get(String id) {
        return id == null ? null : ranks.get(id.toLowerCase());
    }

    public List<DonateRank> all() { return new ArrayList<>(ranks.values()); }

    public DonateRank playerRank(OfflinePlayer p) {
        FileConfiguration cfg = plugin.data().load(p.getUniqueId());
        return get(cfg.getString("donate.rank"));
    }

    public DonateRank playerRank(String name) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        return playerRank(op);
    }

    /** Honour rule "lower donate not given if higher is already active". */
    public boolean give(OfflinePlayer p, String id) {
        DonateRank target = get(id);
        if (target == null) return false;
        DonateRank cur = playerRank(p);
        if (cur != null && cur.weight() > target.weight()) {
            return false;
        }
        FileConfiguration cfg = plugin.data().load(p.getUniqueId());
        cfg.set("donate.rank", target.id());
        cfg.set("donate.given-at", System.currentTimeMillis());
        plugin.data().save(p.getUniqueId(), cfg);
        if (p instanceof Player online) applyAttachment(online);
        return true;
    }

    public void reset(OfflinePlayer p) {
        FileConfiguration cfg = plugin.data().load(p.getUniqueId());
        cfg.set("donate.rank", null);
        plugin.data().save(p.getUniqueId(), cfg);
        if (p instanceof Player online) applyAttachment(online);
    }

    public void set(OfflinePlayer p, String id) {
        DonateRank target = get(id);
        if (target == null) return;
        FileConfiguration cfg = plugin.data().load(p.getUniqueId());
        cfg.set("donate.rank", target.id());
        plugin.data().save(p.getUniqueId(), cfg);
        if (p instanceof Player online) applyAttachment(online);
    }

    public void openGui(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 54, Msg.gradient("ArisDonate", 0xFFD700, 0xFF6A00).decoration(TextDecoration.BOLD, true));
        ItemStack filler = ItemUtil.build(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);
        for (DonateRank r : ranks.values()) {
            if (r.staff()) continue;
            int slot = r.guiSlot();
            if (slot < 0 || slot >= 54) continue;
            List<String> lore = new ArrayList<>();
            lore.addAll(r.description());
            lore.add("");
            lore.add("<gray>Weight: <white>" + r.weight());
            lore.add("<gray>Homes: <white>" + r.homeLimit() + " <gray>Regions: <white>" + r.regionLimit());
            if (r.kitId() != null) lore.add("<gray>Kit: <yellow>/kit " + r.kitId());
            ItemStack it = ItemUtil.build(r.shulker(), r.gradientName(), lore);
            it = ItemUtil.tagged(it, plugin.keyDonateRank(), r.id());
            inv.setItem(slot, it);
        }
        viewer.openInventory(inv);
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { applyAttachment(e.getPlayer()); }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        PermissionAttachment a = attachments.remove(e.getPlayer().getUniqueId());
        if (a != null) e.getPlayer().removeAttachment(a);
    }

    private void applyAttachment(Player p) {
        PermissionAttachment a = attachments.remove(p.getUniqueId());
        if (a != null) p.removeAttachment(a);
        DonateRank rank = playerRank(p);
        if (rank == null) return;
        PermissionAttachment att = p.addAttachment(plugin);
        for (String node : rank.permissions()) att.setPermission(node, true);
        attachments.put(p.getUniqueId(), att);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getType() != InventoryType.CHEST) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.equals(GUI_TITLE)) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it == null) return;
        String id = ItemUtil.getTag(it, plugin.keyDonateRank());
        if (id == null) return;
        DonateRank rank = get(id);
        if (rank == null) return;
        p.sendMessage(Msg.prefix().append(Msg.mm("<gray>Visit <click:url:https://aris.world/donate><grad:#FFD700:#FF6A00>aris.world/donate</grad></click> to buy <b>" + rank.displayName() + "</b>.")));
    }
}
