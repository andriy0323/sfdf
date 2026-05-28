package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.kit.Kit;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public KitCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length < 1) { plugin.kits().openGui(p); return true; }
        Kit k = plugin.kits().get(args[0]);
        if (k == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Unknown kit.</red>"))); return true; }
        plugin.kits().give(p, k, false);
        return true;
    }
}
