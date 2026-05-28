package me.ariscore.kit;

import me.ariscore.ArisCorePlugin;
import me.ariscore.donate.DonateRank;
import me.ariscore.util.ItemUtil;
import me.ariscore.util.Msg;
import me.ariscore.util.TimeUtil;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Kits with cooldowns (2h to 20+ days). Each kit is keyed by id and contains a list of item materials.
 * /free -> starter kit. /kits -> open GUI. Donate-only kits accessible only by matching DonateRank.
 */
public class KitManager implements Listener {

    private static final String GUI_TITLE = "ArisKits";

    private final ArisCorePlugin plugin;
    private final LinkedHashMap<String, Kit> kits = new LinkedHashMap<>();

    public KitManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "kits.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        kits.clear();
        ConfigurationSection root = cfg.getConfigurationSection("kits");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;
            List<ItemStack> contents = new ArrayList<>();
            List<String> raw = sec.getStringList("items");
            for (String line : raw) {
                String[] parts = line.split("\\s+");
                Material mat;
                int amount = 1;
                try { mat = Material.valueOf(parts[0].toUpperCase()); } catch (Exception ex) { continue; }
                if (parts.length > 1) try { amount = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                contents.add(new ItemStack(mat, amount));
            }
            kits.put(id.toLowerCase(), new Kit(
                    id.toLowerCase(),
                    sec.getString("name", id),
                    sec.getString("start-hex", "FFFFFF"),
                    sec.getString("end-hex", "FFFFFF"),
                    parseMat(sec.getString("icon", "CHEST")),
                    sec.getStringList("description"),
                    contents,
                    TimeUtil.parseDuration(sec.getString("cooldown", "2h")),
                    sec.getString("donate-required"),
                    sec.getBoolean("starter", false),
                    sec.getInt("gui-slot", 0)
            ));
        }
    }

    private Material parseMat(String s) { try { return Material.valueOf(s); } catch (Exception e) { return Material.CHEST; } }

    public List<Kit> all() { return new ArrayList<>(kits.values()); }
    public Kit get(String id) { return id == null ? null : kits.get(id.toLowerCase()); }

    public void tryGiveStarter(Player p, boolean silent) {
        Kit starter = null;
        for (Kit k : kits.values()) if (k.starter()) { starter = k; break; }
        if (starter == null) return;
        Long given = plugin.data().load(p).getLong("kits.starter-given", 0L);
        if (given != 0L) return;
        give(p, starter, true);
        FileConfiguration cfg = plugin.data().load(p);
        cfg.set("kits.starter-given", System.currentTimeMillis());
        plugin.data().save(p, cfg);
        if (!silent) p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Starter kit issued.</grad>")));
    }

    public void giveStarterCmd(Player p) {
        FileConfiguration cfg = plugin.data().load(p);
        if (cfg.getLong("kits.starter-given", 0L) > 0) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>You already claimed the starter kit.</red>")));
            return;
        }
        tryGiveStarter(p, false);
    }

    public void give(Player p, Kit kit, boolean ignoreCooldown) {
        if (kit == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Unknown kit.</red>"))); return; }
        if (kit.donateRequired() != null) {
            DonateRank rank = plugin.donates().playerRank(p);
            DonateRank req = plugin.donates().get(kit.donateRequired());
            if (rank == null || req == null || rank.weight() < req.weight()) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>Requires donate: <b>" + (req == null ? kit.donateRequired() : req.displayName()) + "</b></red>")));
                return;
            }
        }
        if (!ignoreCooldown) {
            long ready = plugin.data().load(p).getLong("kits.cd." + kit.id(), 0L);
            if (ready > System.currentTimeMillis()) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>Cooldown: <yellow>" + TimeUtil.formatLeft(ready - System.currentTimeMillis()) + "</yellow></red>")));
                return;
            }
        }
        for (ItemStack it : kit.items()) p.getInventory().addItem(it.clone());
        if (!ignoreCooldown) {
            FileConfiguration cfg = plugin.data().load(p);
            cfg.set("kits.cd." + kit.id(), System.currentTimeMillis() + kit.cooldownMs());
            plugin.data().save(p, cfg);
        }
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Kit <b>" + kit.displayName() + "</b> received.</grad>")));
    }

    public void openGui(Player viewer) {
        Inventory inv = Bukkit.createInventory(viewer, 54, Msg.gradient("ArisKits", 0x7CFC00, 0x32CD32).decoration(TextDecoration.BOLD, true));
        ItemStack filler = ItemUtil.build(Material.LIME_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);
        for (Kit k : kits.values()) {
            int slot = k.guiSlot();
            if (slot < 0 || slot >= 54) continue;
            List<String> lore = new ArrayList<>(k.description());
            lore.add("");
            long ready = plugin.data().load(viewer).getLong("kits.cd." + k.id(), 0L);
            if (ready > System.currentTimeMillis())
                lore.add("<gray>Cooldown: <red>" + TimeUtil.formatLeft(ready - System.currentTimeMillis()));
            else
                lore.add("<gray>Status: <green>Ready");
            if (k.donateRequired() != null) lore.add("<gray>Donate: <gold>" + k.donateRequired());
            lore.add("");
            lore.add("<yellow>Click to claim");
            ItemStack it = ItemUtil.build(k.icon(),
                    "<grad:#" + k.startHex() + ":#" + k.endHex() + "><b>" + k.displayName() + "</b></grad>", lore);
            it = ItemUtil.tagged(it, plugin.keyKitId(), k.id());
            inv.setItem(slot, it);
        }
        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.equals(GUI_TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack cur = e.getCurrentItem();
        if (cur == null || cur.getItemMeta() == null) return;
        String id = cur.getItemMeta().getPersistentDataContainer().get(plugin.keyKitId(), PersistentDataType.STRING);
        if (id == null) return;
        Kit kit = get(id);
        give(p, kit, false);
        p.closeInventory();
    }
}
