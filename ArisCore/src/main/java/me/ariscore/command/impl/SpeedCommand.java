package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpeedCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public SpeedCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("ariscore.speed")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        int amount = 1;
        if (args.length >= 1) { try { amount = Integer.parseInt(args[0]); } catch (Exception ignored) {} }
        amount = Math.max(0, Math.min(10, amount));
        float val = amount / 10f;
        if (p.isFlying() || p.getAllowFlight()) p.setFlySpeed(val);
        else p.setWalkSpeed(val);
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Speed = " + amount + "</grad>")));
        return true;
    }
}
