package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public HealCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("ariscore.heal")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        p.setHealth(p.getAttribute(Attribute.MAX_HEALTH).getValue());
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Healed.</grad>")));
        return true;
    }
}
