package me.ariscore.auth;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Whitelist-based OP system: only nicknames listed in plugins/ArisCore/operators.yml
 * may ever be OP. All other /op /deop commands are intercepted and refused
 * regardless of who issues them (in-game or console). Hacker-style commands
 * are also blocked here.
 *
 * /oper reload re-reads operators.yml and re-applies the OP flag to everyone.
 */
public class OpProtection implements Listener {

    private static final Set<String> BANNED_COMMANDS = Set.of(
            "stop", "restart", "reload", "rl", "save-all", "save-off", "save-on",
            "ban-ip", "pardon-ip", "minecraft:stop", "minecraft:reload",
            "essentials:reload", "pl", "plugins", "version", "ver",
            "execute", "minecraft:execute", "fill", "minecraft:fill",
            "setblock", "minecraft:setblock", "summon", "minecraft:summon",
            "datapack", "minecraft:datapack", "function", "minecraft:function",
            "scriptload", "scriptunload", "scriptreload",
            "rg", "wand"
    );

    private final ArisCorePlugin plugin;
    private final Set<String> allowed = new HashSet<>();
    private final File file;

    public OpProtection(ArisCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "operators.yml");
        load();
    }

    public Set<String> allowed() { return allowed; }

    public void load() {
        allowed.clear();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<String> list = cfg.getStringList("operators");
        for (String n : list) allowed.add(n.toLowerCase());
        if (allowed.isEmpty()) {
            allowed.add("andryshaoff");
            cfg.set("operators", List.of("AndryshaOff"));
            try { cfg.save(file); } catch (IOException ignored) {}
        }
    }

    public boolean isAllowed(String name) {
        return name != null && allowed.contains(name.toLowerCase());
    }

    public void enforceOnStartup() {
        // De-op anyone not on the list, OP everyone on the list.
        for (OfflinePlayer op : Bukkit.getOperators()) {
            if (op.getName() == null) continue;
            if (!isAllowed(op.getName())) op.setOp(false);
        }
        for (String name : allowed) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (!op.isOp()) op.setOp(true);
        }
    }

    public void reload() {
        load();
        enforceOnStartup();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (isAllowed(p.getName())) {
            if (!p.isOp()) p.setOp(true);
        } else if (p.isOp()) {
            p.setOp(false);
            plugin.getLogger().warning("De-opped " + p.getName() + " (not in operators.yml)");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String first = stripFirst(e.getMessage());
        Player p = e.getPlayer();

        // /op nick — only allowed if SENDER is in operators.yml AND target is in operators.yml.
        if (first.equals("op") || first.equals("minecraft:op")) {
            e.setCancelled(true);
            if (!isAllowed(p.getName())) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>You cannot use /op.</red>")));
                plugin.getLogger().warning("Blocked /op from " + p.getName());
                return;
            }
            String target = arg(e.getMessage(), 1);
            if (target == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>/op &lt;player&gt;</red>"))); return; }
            if (!isAllowed(target)) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>Target is not in operators.yml. Add them first and /oper reload.</red>")));
                return;
            }
            Bukkit.getOfflinePlayer(target).setOp(true);
            p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Granted OP to " + target + ".</grad>")));
            return;
        }

        // /deop nick — only operators.yml senders.
        if (first.equals("deop") || first.equals("minecraft:deop")) {
            e.setCancelled(true);
            if (!isAllowed(p.getName())) {
                p.sendMessage(Msg.prefix().append(Msg.mm("<red>You cannot use /deop.</red>")));
                return;
            }
            String target = arg(e.getMessage(), 1);
            if (target == null) { p.sendMessage(Msg.prefix().append(Msg.mm("<red>/deop &lt;player&gt;</red>"))); return; }
            Bukkit.getOfflinePlayer(target).setOp(false);
            p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#FF6A00:#FF0000>De-opped " + target + ".</grad>")));
            return;
        }

        // Block hacker-style commands for everyone except listed operators.
        if (BANNED_COMMANDS.contains(first) && !isAllowed(p.getName())) {
            e.setCancelled(true);
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>This command is blocked.</red>")));
            plugin.getLogger().warning("Blocked /" + first + " from " + p.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onConsole(ServerCommandEvent e) {
        String first = e.getCommand().split(" ")[0].toLowerCase();
        if (first.equals("op") || first.equals("minecraft:op")) {
            String target = arg("/" + e.getCommand(), 1);
            if (target != null && !isAllowed(target)) {
                e.setCancelled(true);
                plugin.getLogger().warning("Blocked console /op " + target + " (not in operators.yml)");
            }
        }
    }

    private static String stripFirst(String message) {
        String s = message.startsWith("/") ? message.substring(1) : message;
        int sp = s.indexOf(' ');
        return (sp < 0 ? s : s.substring(0, sp)).toLowerCase();
    }

    private static String arg(String message, int idx) {
        String[] parts = message.split("\\s+");
        return parts.length > idx ? parts[idx] : null;
    }
}
