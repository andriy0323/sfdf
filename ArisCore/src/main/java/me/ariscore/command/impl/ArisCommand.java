package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /a              -> balance of self
 * /a <player>     -> balance of player
 * /a give <player> <amount>
 * /a giveall <amount>
 * /a set <player> <amount>
 * /a reset <player>
 * /a pay <player> <amount>
 */
public class ArisCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public ArisCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                int bal = (int) plugin.economy().balance(p);
                sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Balance: <yellow>" + bal + " aris</yellow></gray>")));
            } else sender.sendMessage("/a <give|giveall|set|reset|pay> ...");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> {
                if (!sender.hasPermission("ariscore.admin")) { deny(sender); return true; }
                if (args.length < 3) { sender.sendMessage("/a give <player> <amount>"); return true; }
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                double amt = parse(args[2]);
                plugin.economy().give(t.getUniqueId(), amt);
                sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Gave " + (int) amt + " aris to " + args[1] + ".</grad>")));
            }
            case "giveall" -> {
                if (!sender.hasPermission("ariscore.admin")) { deny(sender); return true; }
                if (args.length < 2) { sender.sendMessage("/a giveall <amount>"); return true; }
                double amt = parse(args[1]);
                plugin.economy().giveAllOnline(amt);
                Bukkit.broadcast(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Everyone online received <yellow>" + (int) amt + "</yellow> aris.</grad>")));
            }
            case "set" -> {
                if (!sender.hasPermission("ariscore.admin")) { deny(sender); return true; }
                if (args.length < 3) { sender.sendMessage("/a set <player> <amount>"); return true; }
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                double amt = parse(args[2]);
                plugin.economy().set(t.getUniqueId(), amt);
                sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Set <white>" + args[1] + "</white> to <yellow>" + (int) amt + "</yellow> aris.")));
            }
            case "reset" -> {
                if (!sender.hasPermission("ariscore.admin")) { deny(sender); return true; }
                if (args.length < 2) { sender.sendMessage("/a reset <player>"); return true; }
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                plugin.economy().reset(t.getUniqueId());
                sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Reset <white>" + args[1] + "</white>.</gray>")));
            }
            case "pay" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
                if (args.length < 3) { p.sendMessage("/a pay <player> <amount>"); return true; }
                Player to = Bukkit.getPlayerExact(args[1]);
                if (to == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Player not online.</red>"))); return true; }
                double amt = parse(args[2]);
                if (!plugin.economy().take(p.getUniqueId(), amt)) {
                    p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not enough aris.</red>"))); return true;
                }
                plugin.economy().give(to.getUniqueId(), amt);
                p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Sent " + (int) amt + " aris to " + to.getName() + "</grad>")));
                to.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Received " + (int) amt + " aris from " + p.getName() + "</grad>")));
            }
            default -> {
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
                int bal = (int) plugin.economy().balance(t.getUniqueId());
                sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>" + args[0] + "'s balance: <yellow>" + bal + " aris</yellow>")));
            }
        }
        return true;
    }

    private static double parse(String s) {
        try { return Double.parseDouble(s); } catch (Exception ex) { return 0; }
    }

    private static void deny(CommandSender s) {
        s.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>")));
    }
}
