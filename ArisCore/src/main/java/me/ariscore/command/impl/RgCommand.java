package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.region.Region;
import me.ariscore.region.RegionManager;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /rg                              - clickable menu
 * /rg info                         - info about region you stand in (also /rg i)
 * /rg flag                         - list flags clickable
 * /rg flag <name> <true|false>
 * /rg addowner <player>
 * /rg addmember <player>
 * /rg delmember <player>
 * /rg rename <new>
 * /rg list
 */
public class RgCommand implements CommandExecutor {

    private final ArisCorePlugin plugin;
    public RgCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) { showMenu(p); return true; }
        String sub = args[0].toLowerCase();
        Region here = plugin.regions().regionAt(p.getLocation());

        switch (sub) {
            case "i", "info" -> {
                if (here == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not in a region.</red>"))); return true; }
                showInfo(p, here);
            }
            case "flag" -> {
                if (here == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Stand in your region first.</red>"))); return true; }
                if (!here.isAllowed(p.getUniqueId())) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not your region.</red>"))); return true; }
                if (args.length < 2) { showFlags(p, here); return true; }
                if (args.length < 3) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>/rg flag &lt;name&gt; &lt;true|false&gt;</red>"))); return true; }
                here.setFlag(args[1], Boolean.parseBoolean(args[2]));
                plugin.regions().save();
                p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Flag <yellow>" + args[1] + "</yellow> = <white>" + args[2] + "</white>.</grad>")));
            }
            case "addowner" -> {
                requireOwner(p, here);
                if (here == null) return true;
                if (args.length < 2) { p.sendMessage("/rg addowner <player>"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                here.members().add(op.getUniqueId());
                plugin.regions().save();
                p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Added owner <b>" + args[1] + "</b>.</grad>")));
            }
            case "addmember" -> {
                requireOwner(p, here);
                if (here == null) return true;
                if (args.length < 2) { p.sendMessage("/rg addmember <player>"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                here.members().add(op.getUniqueId());
                plugin.regions().save();
                p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Added member <b>" + args[1] + "</b>.</grad>")));
            }
            case "delmember" -> {
                requireOwner(p, here);
                if (here == null) return true;
                if (args.length < 2) { p.sendMessage("/rg delmember <player>"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                here.members().remove(op.getUniqueId());
                plugin.regions().save();
                p.sendMessage(Msg.prefix().append(Msg.mm("<gray>Removed member.</gray>")));
            }
            case "rename" -> {
                requireOwner(p, here);
                if (here == null) return true;
                if (args.length < 2) { p.sendMessage("/rg rename <name>"); return true; }
                here.setName(args[1]);
                plugin.regions().save();
                p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Renamed to <b>" + args[1] + "</b>.</grad>")));
            }
            case "list" -> {
                List<Region> rs = plugin.regions().ownedBy(p.getUniqueId());
                p.sendMessage(Msg.prefix().append(Msg.mm("<gray>Your regions: <white>" + rs.size())));
                for (Region r : rs) p.sendMessage(Msg.mm("  <gray>- <yellow>" + r.name() + "</yellow> <dark_gray>at <white>" + r.x() + ", " + r.y() + ", " + r.z() + "</white>"));
            }
            default -> showMenu(p);
        }
        return true;
    }

    private void requireOwner(Player p, Region r) {
        if (r == null) p.sendMessage(Msg.prefix().append(Msg.mm("<red>Stand in your region first.</red>")));
        else if (!r.owner().equals(p.getUniqueId())) p.sendMessage(Msg.prefix().append(Msg.mm("<red>Not your region.</red>")));
    }

    private void showMenu(Player p) {
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FFD700:#FF6A00><b>Region menu</b></grad>")));
        p.sendMessage(Msg.mm("  <click:run:/rg info><grad:#7CFC00:#32CD32>[info]</grad></click>" +
                " <click:run:/rg flag><grad:#7CFC00:#32CD32>[flags]</grad></click>" +
                " <click:suggest:/rg addowner ><grad:#7CFC00:#32CD32>[addowner]</grad></click>" +
                " <click:suggest:/rg addmember ><grad:#7CFC00:#32CD32>[addmember]</grad></click>" +
                " <click:suggest:/rg rename ><grad:#7CFC00:#32CD32>[rename]</grad></click>" +
                " <click:run:/rg list><grad:#7CFC00:#32CD32>[list]</grad></click>"));
    }

    private void showInfo(Player p, Region r) {
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FFD700:#FF6A00><b>" + r.name() + "</b></grad>")));
        p.sendMessage(Msg.mm("  <gray>Owner: <white>" + Bukkit.getOfflinePlayer(r.owner()).getName()));
        p.sendMessage(Msg.mm("  <gray>Members: <white>" + r.members().size()));
        p.sendMessage(Msg.mm("  <gray>Radius: <white>" + r.radius()));
    }

    private void showFlags(Player p, Region r) {
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FFD700:#FF6A00><b>Flags</b></grad> <gray>(click to toggle)")));
        for (String f : RegionManager.FLAGS) {
            boolean cur = r.flag(f, false);
            String click = "<click:run:/rg flag " + f + " " + (!cur) + ">";
            String state = cur ? "<green>ON" : "<red>OFF";
            p.sendMessage(Msg.mm("  " + click + "<gray>[" + f + "] " + state + "</click>"));
        }
    }
}
