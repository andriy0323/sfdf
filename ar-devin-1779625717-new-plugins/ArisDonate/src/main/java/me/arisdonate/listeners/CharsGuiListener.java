package me.arisdonate.listeners;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.gui.CharsCatalog;
import me.arisdonate.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Обрабатывает клики в GUI /chars: накидает чары на предмет в правой руке.
 *
 *   ЛКМ        — наложить уровень I
 *   ПКМ        — наложить максимальный уровень
 *   Shift+клик — снять чару (если она есть)
 */
public class CharsGuiListener implements Listener {

    private final ArisDonatePlugin plugin;

    public CharsGuiListener(ArisDonatePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        Component title = e.getView().title();
        if (title == null) return;
        if (!Msg.stripFormatting(title).contains("ЧАРЫ")) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack icon = e.getCurrentItem();
        if (icon == null || icon.getType().isAir()) return;
        ItemMeta iconMeta = icon.getItemMeta();
        if (iconMeta == null) return;
        String key = iconMeta.getPersistentDataContainer().get(plugin.keyCharId(), PersistentDataType.STRING);
        if (key == null) return;

        CharsCatalog.Entry entry = CharsCatalog.get(key);
        if (entry == null) {
            p.sendMessage(Msg.parse("&cНеизвестная чара."));
            return;
        }

        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            p.sendMessage(Msg.parse("&cВозьми инструмент в правую руку."));
            return;
        }

        if (e.isShiftClick()) {
            removeEnchant(p, held, entry);
        } else if (e.isRightClick()) {
            applyEnchant(p, held, entry, entry.maxLevel());
        } else {
            applyEnchant(p, held, entry, 1);
        }

        plugin.getCharsGui().open(p);
    }

    private void applyEnchant(Player p, ItemStack item, CharsCatalog.Entry entry, int level) {
        if (entry.custom() && entry.key().equalsIgnoreCase(CharsCatalog.TELEKINESIS_KEY)) {
            applyTelekinesis(p, item, level);
            return;
        }
        Enchantment en = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.key()));
        if (en == null) {
            p.sendMessage(Msg.parse("&cЧара &e" + entry.ruName() + " &cнедоступна в этой версии."));
            return;
        }
        ItemMeta im = item.getItemMeta();
        if (im == null) {
            p.sendMessage(Msg.parse("&cЭтот предмет нельзя зачаровать."));
            return;
        }
        im.addEnchant(en, level, true);
        item.setItemMeta(im);
        p.sendMessage(Msg.parse("&aНаложил &b" + entry.ruName() + " &7уровень &f" + roman(level) + "&7 на " + describe(item)));
    }

    private void applyTelekinesis(Player p, ItemStack item, int level) {
        ItemMeta im = item.getItemMeta();
        if (im == null) {
            p.sendMessage(Msg.parse("&cЭтот предмет нельзя зачаровать."));
            return;
        }
        if (level <= 0) {
            removeTelekinesis(p, item, im);
            return;
        }
        im.getPersistentDataContainer().set(plugin.keyTelekinesis(), PersistentDataType.BYTE, (byte) 1);
        List<Component> lore = new ArrayList<>(im.lore() == null ? new ArrayList<>() : im.lore());
        Component tag = Msg.parse("&dТелепатия &7I");
        boolean hasTag = lore.stream().anyMatch(c -> Msg.stripFormatting(c).contains("Телепатия"));
        if (!hasTag) lore.add(tag);
        im.lore(lore);
        item.setItemMeta(im);
        p.sendMessage(Msg.parse("&aНаложил &dТелепатию&7 на " + describe(item)));
    }

    private void removeEnchant(Player p, ItemStack item, CharsCatalog.Entry entry) {
        if (entry.custom() && entry.key().equalsIgnoreCase(CharsCatalog.TELEKINESIS_KEY)) {
            ItemMeta im = item.getItemMeta();
            if (im == null) return;
            removeTelekinesis(p, item, im);
            return;
        }
        Enchantment en = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.key()));
        if (en == null) return;
        ItemMeta im = item.getItemMeta();
        if (im == null) return;
        if (!im.hasEnchant(en)) {
            p.sendMessage(Msg.parse("&7Этой чары и так нет."));
            return;
        }
        im.removeEnchant(en);
        item.setItemMeta(im);
        p.sendMessage(Msg.parse("&7Снял &b" + entry.ruName() + " &7с " + describe(item)));
    }

    private void removeTelekinesis(Player p, ItemStack item, ItemMeta im) {
        boolean had = Boolean.TRUE.equals(im.getPersistentDataContainer().has(plugin.keyTelekinesis(), PersistentDataType.BYTE));
        im.getPersistentDataContainer().remove(plugin.keyTelekinesis());
        if (im.lore() != null) {
            List<Component> lore = new ArrayList<>(im.lore());
            lore.removeIf(c -> Msg.stripFormatting(c).contains("Телепатия"));
            im.lore(lore);
        }
        item.setItemMeta(im);
        p.sendMessage(Msg.parse(had ? "&7Снял &dТелепатию&7 с " + describe(item)
                                    : "&7Этой чары и так нет."));
    }

    private String describe(ItemStack item) {
        return item.getType().name().toLowerCase().replace('_', ' ');
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
