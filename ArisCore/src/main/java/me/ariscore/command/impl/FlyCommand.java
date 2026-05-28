package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public FlyCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("ariscore.fly")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        p.setAllowFlight(!p.getAllowFlight());
        p.setFlying(p.getAllowFlight());
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Fly " + (p.getAllowFlight() ? "ON" : "OFF") + "</grad>")));
        return true;
    }
}
