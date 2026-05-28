package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final ArisCorePlugin plugin;
    public WarpCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<gray>Warps: <white>" + String.join(", ", plugin.teleport().warpNames()))));
            return true;
        }
        Location loc = plugin.teleport().getWarp(args[0]);
        if (loc == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Warp not found.</red>"))); return true; }
        p.teleport(loc);
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Warped to <b>" + args[0] + "</b></grad>")));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>(plugin.teleport().warpNames());
    }
}
