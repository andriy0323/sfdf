package me.arisdonate.gui;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI каталога чаров.
 *
 * Хранит в lore текущий уровень чары на предмете в руке и максимальный
 * доступный уровень. Левый клик — накинуть уровень 1, правый клик —
 * максимальный уровень, Shift+клик — снять чару.
 */
public class CharsGui {

    public static final String TITLE_RAW = "ЧАРЫ";

    private final ArisDonatePlugin plugin;

    public CharsGui(ArisDonatePlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54,
                Msg.parse("<grad:#9F70FD:#5A2E96>★ ЧАРЫ ★</grad>"));

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Component.text(" "));
        pane.setItemMeta(pm);
        for (int i = 0; i < 9; i++) inv.setItem(i, pane);

        ItemStack info = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta in = info.getItemMeta();
        in.displayName(Msg.parse("<grad:#9F70FD:#5A2E96>★ Чары ★</grad>"));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Msg.parse("&7Возьми инструмент в руку и кликни по чаре:"));
        infoLore.add(Msg.parse(" &f• &eЛКМ &7— наложить I уровень"));
        infoLore.add(Msg.parse(" &f• &eПКМ &7— наложить максимальный уровень"));
        infoLore.add(Msg.parse(" &f• &eShift+клик &7— снять чару"));
        infoLore.add(Component.empty());
        infoLore.add(Msg.parse("&8Поддерживаются ваниль-чары + &dТелепатия&8."));
        in.lore(infoLore);
        info.setItemMeta(in);
        inv.setItem(4, info);

        int slot = 9;
        ItemStack held = p.getInventory().getItemInMainHand();
        for (CharsCatalog.Entry e : CharsCatalog.all()) {
            if (slot >= 54) break;
            inv.setItem(slot++, buildIcon(e, held));
        }

        p.openInventory(inv);
    }

    private ItemStack buildIcon(CharsCatalog.Entry e, ItemStack held) {
        ItemStack icon = new ItemStack(e.icon());
        ItemMeta im = icon.getItemMeta();

        String prefix = e.custom() ? "&d" : "&b";
        im.displayName(Msg.parse(prefix + "&l" + e.ruName() + " &7(макс. &f" + roman(e.maxLevel()) + "&7)"));

        List<Component> lore = new ArrayList<>();
        lore.add(Msg.parse("&8&m                        "));
        lore.add(Msg.parse("&7" + e.description()));
        lore.add(Component.empty());

        int current = currentLevel(e, held);
        if (current > 0) {
            lore.add(Msg.parse("&aСейчас на предмете: &f" + roman(current)));
        } else {
            lore.add(Msg.parse("&8Сейчас на предмете: &7нет"));
        }

        lore.add(Component.empty());
        lore.add(Msg.parse("&7Макс. уровень: &f" + roman(e.maxLevel())));
        lore.add(Component.empty());
        lore.add(Msg.parse("&eЛКМ &7— I уровень"));
        lore.add(Msg.parse("&eПКМ &7— макс."));
        lore.add(Msg.parse("&eShift+клик &7— снять"));
        lore.add(Msg.parse("&8&m                        "));
        im.lore(lore);

        im.getPersistentDataContainer().set(plugin.keyCharId(),
                PersistentDataType.STRING, e.key());

        icon.setItemMeta(im);
        return icon;
    }

    private int currentLevel(CharsCatalog.Entry e, ItemStack held) {
        if (held == null || held.getType().isAir()) return 0;
        if (e.custom()) {
            ItemMeta im = held.getItemMeta();
            if (im == null) return 0;
            Byte b = im.getPersistentDataContainer().get(plugin.keyTelekinesis(), PersistentDataType.BYTE);
            return (b != null && b == (byte) 1) ? 1 : 0;
        }
        org.bukkit.enchantments.Enchantment en = CharsCatalog.vanillaEnchant(e);
        if (en == null) return 0;
        return held.getEnchantmentLevel(en);
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }
}
