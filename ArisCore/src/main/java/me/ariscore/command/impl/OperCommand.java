package me.ariscore.command.impl;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * /oper reload  - reload operators.yml
 * /oper add &lt;name&gt; - add to operators.yml (only if sender is already in the list)
 * /oper remove &lt;name&gt; - remove
 * /oper list - print operators.yml
 */
public class OperCommand implements CommandExecutor {
    private final ArisCorePlugin plugin;
    public OperCommand(ArisCorePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p && !plugin.ops().isAllowed(p.getName())) {
            sender.sendMessage(Msg.prefix().append(Msg.mm("<red>You cannot use this command.</red>")));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Usage: /oper &lt;reload|add|remove|list&gt; [name]</gray>")));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.ops().reload();
                sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>operators.yml reloaded. " + plugin.ops().allowed().size() + " entries.</grad>")));
            }
            case "list" -> sender.sendMessage(Msg.prefix().append(Msg.mm("<gray>Operators: <white>" + String.join(", ", plugin.ops().allowed()))));
            case "add" -> {
                if (args.length < 2) { sender.sendMessage(Msg.prefix().append(Msg.mm("<red>/oper add &lt;name&gt;</red>"))); return true; }
                modify(args[1], true, sender);
            }
            case "remove" -> {
                if (args.length < 2) { sender.sendMessage(Msg.prefix().append(Msg.mm("<red>/oper remove &lt;name&gt;</red>"))); return true; }
                modify(args[1], false, sender);
            }
            default -> sender.sendMessage(Msg.prefix().append(Msg.mm("<red>Unknown sub-command.</red>")));
        }
        return true;
    }

    private void modify(String name, boolean add, CommandSender sender) {
        File f = new File(plugin.getDataFolder(), "operators.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        List<String> list = new ArrayList<>(cfg.getStringList("operators"));
        list.removeIf(n -> n.equalsIgnoreCase(name));
        if (add) list.add(name);
        cfg.set("operators", list);
        try { cfg.save(f); } catch (IOException e) { sender.sendMessage(Msg.prefix().append(Msg.mm("<red>save error: " + e.getMessage() + "</red>"))); }
        plugin.ops().reload();
        sender.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>" + (add ? "Added " : "Removed ") + name + ".</grad>")));
    }
}
