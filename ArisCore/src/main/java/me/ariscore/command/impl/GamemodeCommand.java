package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    private final String forced;
    public GamemodeCommand(ArisCorePlugin plugin, String forced) { this.plugin = plugin; this.forced = forced; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!p.hasPermission("ariscore.gamemode")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
        String mode = forced;
        if (mode == null && args.length >= 1) mode = args[0];
        if (mode == null) { p.sendMessage("/gm <s|c|a|sp>"); return true; }
        GameMode gm = switch (mode.toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
        if (gm == null) { p.sendMessage("Unknown mode."); return true; }
        p.setGameMode(gm);
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Gamemode: " + gm.name() + "</grad>")));
        return true;
    }
}
