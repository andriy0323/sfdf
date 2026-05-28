package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoginCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public LoginCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length != 1) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Usage: /login &lt;password&gt;</red>")));
            return true;
        }
        plugin.auth().login(p, args[0]);
        return true;
    }
}
