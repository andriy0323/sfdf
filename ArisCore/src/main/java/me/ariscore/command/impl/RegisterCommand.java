package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegisterCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public RegisterCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length != 2) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Usage: /register &lt;password&gt; &lt;password&gt;</red>")));
            return true;
        }
        plugin.auth().register(p, args[0], args[1]);
        return true;
    }
}
