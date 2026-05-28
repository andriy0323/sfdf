package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public ReloadCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ariscore.admin")) { sender.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        plugin.reloadConfig();
        plugin.donates().reload();
        plugin.kits().reload();
        plugin.shop().reload();
        plugin.ops().reload();
        sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>ArisCore reloaded.</grad>")));
        return true;
    }
}
