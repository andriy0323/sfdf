package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.donate.DonateRank;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * /ad give <player> <rank> - give rank (lower donate not given over higher)
 * /ad set  <player> <rank> - force set rank
 * /ad reset <player>       - clear rank
 * /ad list                 - list ranks
 * /ad reload               - reload donates.yml
 */
public class ArisDonateAdminCommand implements CommandExecutor {

    private final ArisCorePlugin plugin;
    public ArisDonateAdminCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ariscore.admin")) {
            sender.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true;
        }
        if (args.length == 0) { help(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) { sender.sendMessage("/ad give <player> <rank>"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                boolean ok = plugin.donates().give(op, args[2]);
                sender.sendMessage(Msg.prefix().append(Msg.mm(ok
                        ? "<grad:#7CFC00:#32CD32>Gave <yellow>" + args[2] + "</yellow> to " + args[1] + ".</grad>"
                        : "<red>Player already has a higher donate or rank not found.</red>")));
            }
            case "set" -> {
                if (args.length < 3) { sender.sendMessage("/ad set <player> <rank>"); return true; }
                plugin.donates().set(Bukkit.getOfflinePlayer(args[1]), args[2]);
                sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Forced set.</grad>")));
            }
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage("/ad reset <player>"); return true; }
                plugin.donates().reset(Bukkit.getOfflinePlayer(args[1]));
                sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Reset.</gray>")));
            }
            case "list" -> sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Ranks: <white>" + plugin.donates().all().stream().map(DonateRank::id).collect(Collectors.joining(", ")))));
            case "reload" -> {
                plugin.donates().reload();
                sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>donates.yml reloaded.</grad>")));
            }
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage(Msg.prefix().append(Msg.mm("<gray>/ad &lt;give|set|reset|list|reload&gt; ...</gray>")));
    }
}
