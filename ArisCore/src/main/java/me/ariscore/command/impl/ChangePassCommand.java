package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChangePassCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public ChangePassCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length != 2) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Usage: /changepass &lt;old&gt; &lt;new&gt;</red>")));
            return true;
        }
        plugin.auth().changePassword(p, args[0], args[1]);
        return true;
    }
}
