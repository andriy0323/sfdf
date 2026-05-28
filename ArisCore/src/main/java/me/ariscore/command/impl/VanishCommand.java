package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    private static final Set<UUID> vanished = new HashSet<>();
    public VanishCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("ariscore.vanish")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        if (vanished.add(p.getUniqueId())) {
            for (Player o : Bukkit.getOnlinePlayers()) if (!o.hasPermission("ariscore.vanish.see")) o.hidePlayer(plugin, p);
            p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Vanished.</grad>")));
        } else {
            vanished.remove(p.getUniqueId());
            for (Player o : Bukkit.getOnlinePlayers()) o.showPlayer(plugin, p);
            p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Visible.</grad>")));
        }
        return true;
    }
}
