package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public TpaCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>/tpa &lt;player&gt;</red>"))); return true; }
        Player to = Bukkit.getPlayerExact(args[0]);
        if (to == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Player not online.</red>"))); return true; }
        if (to.equals(p)) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Cannot TPA to yourself.</red>"))); return true; }
        plugin.teleport().sendTpa(p, to);
        return true;
    }
}
