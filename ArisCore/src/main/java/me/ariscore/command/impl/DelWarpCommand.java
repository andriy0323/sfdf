package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DelWarpCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public DelWarpCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) { sender.sendMessage("/delwarp <name>"); return true; }
        boolean ok = plugin.teleport().delWarp(args[0]);
        sender.sendMessage(Msg.prefix().append(Msg.mm(ok ? "<grad:#FF6A00:#FF0000>Deleted warp.</grad>" : "<red>Not found.</red>")));
        return true;
    }
}
