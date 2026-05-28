package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionBlockCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public RegionBlockCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        p.getInventory().addItem(plugin.regions().regionBlockItem());
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Region Block added.</grad>")));
        return true;
    }
}
