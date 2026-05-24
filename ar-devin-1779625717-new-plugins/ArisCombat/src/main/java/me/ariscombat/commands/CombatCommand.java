package me.ariscombat.commands;

import me.ariscombat.ArisCombatPlugin;
import me.ariscombat.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CombatCommand implements CommandExecutor, TabCompleter {

    private final ArisCombatPlugin plugin;

    public CombatCommand(ArisCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Использование: /ct check <ник> | clear <ник> | reload");
                return true;
            }
            if (plugin.getCombatManager().inCombat(p)) {
                int left = plugin.getCombatManager().secondsLeft(p);
                p.sendMessage(Msg.parse("&cВы в КТ. Осталось: &e" + left + " сек"));
            } else {
                p.sendMessage(Msg.parse("&aВы не в КТ."));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "check" -> {
                if (!sender.hasPermission("ariscombat.check")) {
                    sender.sendMessage(Msg.parse("&cНет прав."));
                    return true;
                }
                if (args.length < 2) { sender.sendMessage(Msg.parse("&7/ct check <ник>")); return true; }
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { sender.sendMessage(Msg.parse("&cИгрок не в сети.")); return true; }
                if (plugin.getCombatManager().inCombat(t)) {
                    int left = plugin.getCombatManager().secondsLeft(t);
                    sender.sendMessage(Msg.parse("&e" + t.getName() + " &cв КТ. Осталось: &e" + left + " сек"));
                } else {
                    sender.sendMessage(Msg.parse("&e" + t.getName() + " &aне в КТ."));
                }
            }
            case "clear" -> {
                if (!sender.hasPermission("ariscombat.admin")) {
                    sender.sendMessage(Msg.parse("&cНет прав."));
                    return true;
                }
                if (args.length < 2) { sender.sendMessage(Msg.parse("&7/ct clear <ник>")); return true; }
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { sender.sendMessage(Msg.parse("&cИгрок не в сети.")); return true; }
                plugin.getCombatManager().clear(t);
                sender.sendMessage(Msg.parse("&aКТ снят с &e" + t.getName()));
            }
            case "reload" -> {
                if (!sender.hasPermission("ariscombat.admin")) {
                    sender.sendMessage(Msg.parse("&cНет прав."));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(Msg.parse("&aArisCombat config перезагружен."));
            }
            default -> sender.sendMessage(Msg.parse("&7/ct | check <ник> | clear <ник> | reload"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>(List.of("check", "clear", "reload"));
            String partial = args[0].toLowerCase(Locale.ROOT);
            out.removeIf(s -> !s.startsWith(partial));
            return out;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("clear"))) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return names;
        }
        return List.of();
    }
}
