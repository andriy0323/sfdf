package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class WarpsCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public WarpsCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Warps: <white>" + String.join(", ", plugin.teleport().warpNames()))));
        return true;
    }
}
