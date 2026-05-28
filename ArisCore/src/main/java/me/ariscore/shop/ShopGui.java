package me.ariscore.shop;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.ItemUtil;
import me.ariscore.util.Msg;
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

/**
 * /shop GUI with blocks priced 100..2500 aris. Loaded from shop.yml.
 */
public class ShopGui implements Listener {

    private static final String GUI_TITLE = "ArisShop";

    private final ArisCorePlugin plugin;
    private final LinkedHashMap<String, ShopItem> items = new LinkedHashMap<>();

    public ShopGui(ArisCorePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "shop.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        items.clear();
        ConfigurationSection root = cfg.getConfigurationSection("items");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;
            Material mat;
            try { mat = Material.valueOf(sec.getString("material", "STONE")); } catch (Exception ex) { continue; }
            items.put(id.toLowerCase(), new ShopItem(
                    id.toLowerCase(),
                    mat,
                    sec.getInt("amount", 1),
                    sec.getDouble("price", 100.0),
                    sec.getInt("slot", 0),
                    sec.getString("name", mat.name()),
                    sec.getString("start-hex", "FFD700"),
                    sec.getString("end-hex", "FF6A00")
            ));
        }
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(p, 54, Msg.gradient("ArisShop", 0xFFD700, 0xFF6A00).decoration(TextDecoration.BOLD, true));
        ItemStack filler = ItemUtil.build(Material.YELLOW_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);
        for (ShopItem si : items.values()) {
            int slot = si.slot();
            if (slot < 0 || slot >= 54) continue;
            List<String> lore = new ArrayList<>();
            lore.add("<gray>Material: <white>" + si.material().name());
            lore.add("<gray>Amount: <white>" + si.amount());
            lore.add("<gray>Price: <yellow>" + (int) si.price() + " aris");
            lore.add("");
            lore.add("<yellow>Left-click to buy 1");
            lore.add("<yellow>Shift-click to buy 64");
            ItemStack it = ItemUtil.build(si.material(),
                    "<grad:#" + si.startHex() + ":#" + si.endHex() + "><b>" + si.displayName() + "</b></grad>", lore);
            it.setAmount(Math.max(1, si.amount()));
            it = ItemUtil.tagged(it, plugin.keyShopItem(), si.id());
            inv.setItem(slot, it);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.equals(GUI_TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack cur = e.getCurrentItem();
        if (cur == null || cur.getItemMeta() == null) return;
        String id = cur.getItemMeta().getPersistentDataContainer().get(plugin.keyShopItem(), PersistentDataType.STRING);
        if (id == null) return;
        ShopItem si = items.get(id);
        if (si == null) return;
        int times = e.isShiftClick() ? 64 : 1;
        double total = si.price() * times;
        if (!plugin.economy().take(p.getUniqueId(), total)) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not enough aris (need <yellow>" + (int) total + "</yellow>).</red>")));
            return;
        }
        ItemStack give = new ItemStack(si.material(), si.amount() * times);
        p.getInventory().addItem(give).forEach((slotIdx, leftover) -> p.getWorld().dropItemNaturally(p.getLocation(), leftover));
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Bought <white>" + (si.amount() * times) + "x " + si.material().name() + "</white> for <yellow>" + (int) total + "</yellow> aris.</grad>")));
    }
}
