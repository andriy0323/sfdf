package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.crate.CrateType;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /dc                       - clickable list of crate types
 * /dc list                  - same
 * /dc place <type>          - place crate at the looked-at block
 * /dc remove                - remove crate at the looked-at block
 * /dc preview <type>        - preview rewards in a GUI
 */
public class DcCommand implements CommandExecutor {

    private final ArisCorePlugin plugin;
    public DcCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            list(p); return true;
        }
        switch (args[0].toLowerCase()) {
            case "place" -> {
                if (args.length < 2) { p.sendMessage("/dc place <type>"); return true; }
                if (!p.hasPermission("ariscore.admin")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
                plugin.crates().place(p, args[1]);
            }
            case "remove" -> {
                if (!p.hasPermission("ariscore.admin")) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>No permission.</red>"))); return true; }
                if (!plugin.crates().removeAtTarget(p)) p.sendMessage(Msg.prefix().append(Msg.mm("<red>No crate at the looked-at block.</red>")));
            }
            case "preview" -> {
                if (args.length < 2) { p.sendMessage("/dc preview <type>"); return true; }
                CrateType type = plugin.crates().type(args[1]);
                if (type == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Unknown crate.</red>"))); return true; }
                plugin.crates().openPreview(p, type);
            }
            default -> list(p);
        }
        return true;
    }

    private void list(Player p) {
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FFD700:#FF6A00><b>Crates</b></grad>")));
        for (CrateType t : plugin.crates().typesList()) {
            p.sendMessage(Msg.mm("  <click:run:/dc preview " + t.id() + "><grad:#" + t.startHex() + ":#" + t.endHex() + ">" + t.displayName() + "</grad></click> <gray>(price " + (int) t.price() + ", donate " + t.donateRequired() + ")"));
        }
        p.sendMessage(Msg.mm("  <click:suggest:/dc place ><gray>[place &lt;type&gt;]</click> <click:run:/dc remove><gray>[remove]</click>"));
    }
}
