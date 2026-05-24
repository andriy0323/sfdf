package me.arisdonate.commands;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.gui.CharsCatalog;
import me.arisdonate.util.Msg;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /chars                        — открыть GUI каталога чаров
 * /chars list                   — текстовый список доступных чар
 * /chars <key> [level]          — напрямую наложить чару на предмет в правой руке
 * /chars remove <key>           — снять чару
 */
public class CharsCommand implements CommandExecutor, TabCompleter {

    private final ArisDonatePlugin plugin;

    public CharsCommand(ArisDonatePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("arisdonate.chars") && !sender.isOp()) {
            sender.sendMessage(Msg.parse("&cНет прав. Нужна арендованная чара-привилегия."));
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Msg.parse("&cТолько для игроков."));
            return true;
        }
        if (args.length == 0) {
            plugin.getCharsGui().open(p);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                p.sendMessage(Msg.parse("&eДоступные чары:"));
                for (CharsCatalog.Entry e : CharsCatalog.all()) {
                    p.sendMessage(Msg.parse(" &f• &b" + e.ruName() + " &7(" + e.key() + ") &8макс. &fI-" + e.maxLevel()));
                }
            }
            case "remove" -> {
                if (args.length < 2) { p.sendMessage(Msg.parse("&7/chars remove <key>")); return true; }
                CharsCatalog.Entry entry = CharsCatalog.get(args[1]);
                if (entry == null) { p.sendMessage(Msg.parse("&cЧара не найдена.")); return true; }
                ItemStack held = p.getInventory().getItemInMainHand();
                if (held == null || held.getType().isAir()) {
                    p.sendMessage(Msg.parse("&cВозьми предмет в руку.")); return true;
                }
                doRemove(p, held, entry);
            }
            default -> {
                CharsCatalog.Entry entry = CharsCatalog.get(sub);
                if (entry == null) { p.sendMessage(Msg.parse("&cЧара не найдена. &7/chars list")); return true; }
                int level = entry.maxLevel();
                if (args.length >= 2) {
                    try {
                        level = Math.max(1, Math.min(Integer.parseInt(args[1]), 10));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(Msg.parse("&cУровень должен быть числом 1..10."));
                        return true;
                    }
                }
                ItemStack held = p.getInventory().getItemInMainHand();
                if (held == null || held.getType().isAir()) {
                    p.sendMessage(Msg.parse("&cВозьми предмет в руку.")); return true;
                }
                doApply(p, held, entry, level);
            }
        }
        return true;
    }

    private void doApply(Player p, ItemStack item, CharsCatalog.Entry entry, int level) {
        if (entry.custom() && entry.key().equalsIgnoreCase(CharsCatalog.TELEKINESIS_KEY)) {
            ItemMeta im = item.getItemMeta();
            if (im == null) { p.sendMessage(Msg.parse("&cЭтот предмет нельзя зачаровать.")); return; }
            im.getPersistentDataContainer().set(plugin.keyTelekinesis(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(im);
            p.sendMessage(Msg.parse("&aНаложил &dТелепатию&7 на предмет."));
            return;
        }
        Enchantment en = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.key()));
        if (en == null) { p.sendMessage(Msg.parse("&cЧара недоступна в этой версии Minecraft.")); return; }
        ItemMeta im = item.getItemMeta();
        if (im == null) { p.sendMessage(Msg.parse("&cЭтот предмет нельзя зачаровать.")); return; }
        im.addEnchant(en, level, true);
        item.setItemMeta(im);
        p.sendMessage(Msg.parse("&aНаложил &b" + entry.ruName() + " &7уровень &f" + level + "&7."));
    }

    private void doRemove(Player p, ItemStack item, CharsCatalog.Entry entry) {
        if (entry.custom() && entry.key().equalsIgnoreCase(CharsCatalog.TELEKINESIS_KEY)) {
            ItemMeta im = item.getItemMeta();
            if (im == null) return;
            im.getPersistentDataContainer().remove(plugin.keyTelekinesis());
            item.setItemMeta(im);
            p.sendMessage(Msg.parse("&7Снял &dТелепатию&7."));
            return;
        }
        Enchantment en = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.key()));
        if (en == null) return;
        ItemMeta im = item.getItemMeta();
        if (im == null) return;
        im.removeEnchant(en);
        item.setItemMeta(im);
        p.sendMessage(Msg.parse("&7Снял &b" + entry.ruName() + "&7."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            out.add("list");
            out.add("remove");
            for (CharsCatalog.Entry e : CharsCatalog.all()) out.add(e.key());
            out.removeIf(s -> !s.startsWith(partial));
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<String> out = new ArrayList<>();
            for (CharsCatalog.Entry e : CharsCatalog.all()) out.add(e.key());
            return out;
        }
        if (args.length == 2) {
            return List.of("1", "2", "3", "4", "5");
        }
        return List.of();
    }
}
