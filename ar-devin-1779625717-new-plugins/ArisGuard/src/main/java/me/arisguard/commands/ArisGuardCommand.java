package me.arisguard.commands;

import me.arisguard.ArisGuardPlugin;
import me.arisguard.managers.Msg;
import me.arisguard.managers.OpGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArisGuardCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("reload", "add", "remove", "list", "status", "unblock");

    private final ArisGuardPlugin plugin;

    public ArisGuardCommand(ArisGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("arisguard.admin") && !sender.isOp()) {
            sender.sendMessage(Msg.parse("&cНет прав."));
            return true;
        }
        if (args.length == 0) {
            usage(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload"  -> doReload(sender);
            case "add"     -> doAdd(sender, args);
            case "remove"  -> doRemove(sender, args);
            case "list"    -> doList(sender);
            case "status"  -> doStatus(sender);
            case "unblock" -> doUnblock(sender, args);
            default        -> usage(sender);
        }
        return true;
    }

    private void usage(CommandSender s) {
        s.sendMessage(Msg.parse("&7/arisguard &freload &7— перечитать config"));
        s.sendMessage(Msg.parse("&7/arisguard &fadd &7<ник> &7— добавить в allowed-ops"));
        s.sendMessage(Msg.parse("&7/arisguard &fremove &7<ник> &7— удалить из allowed-ops"));
        s.sendMessage(Msg.parse("&7/arisguard &flist &7— показать allowed-ops"));
        s.sendMessage(Msg.parse("&7/arisguard &fstatus &7— статус защиты"));
        s.sendMessage(Msg.parse("&7/arisguard &funblock &7<ip> &7— снять временный бан IP"));
    }

    private void doReload(CommandSender s) {
        plugin.reloadConfig();
        plugin.getConnectionGuard().reload();
        plugin.getOpGuard().reload();
        plugin.restartOpScan();
        s.sendMessage(Msg.parse("&aArisGuard: конфиг перезагружен."));
    }

    private void doAdd(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage(Msg.parse("&7/arisguard add <ник>")); return; }
        String name = args[1];
        OpGuard og = plugin.getOpGuard();
        if (og.add(name)) {
            s.sendMessage(Msg.parse("&aДобавлен в allowed-ops: &e" + name));
        } else {
            s.sendMessage(Msg.parse("&7" + name + " &7уже в allowed-ops."));
        }
    }

    private void doRemove(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage(Msg.parse("&7/arisguard remove <ник>")); return; }
        String name = args[1];
        OpGuard og = plugin.getOpGuard();
        if (og.remove(name)) {
            s.sendMessage(Msg.parse("&aУдалён из allowed-ops: &e" + name));
            int n = og.scanAndEnforce();
            if (n > 0) s.sendMessage(Msg.parse("&7Разоп-нуто: " + n));
        } else {
            s.sendMessage(Msg.parse("&7" + name + " &7не найден в allowed-ops."));
        }
    }

    private void doList(CommandSender s) {
        List<String> names = plugin.getOpGuard().list();
        s.sendMessage(Msg.parse("&eAllowed-ops &7(" + names.size() + "):"));
        if (names.isEmpty()) {
            s.sendMessage(Msg.parse("  &8(пусто)"));
        } else {
            for (String n : names) s.sendMessage(Msg.parse("  &7- &f" + n));
        }
    }

    private void doStatus(CommandSender s) {
        var cg = plugin.getConnectionGuard();
        var og = plugin.getOpGuard();
        s.sendMessage(Msg.parse("&eArisGuard &7статус:"));
        s.sendMessage(Msg.parse("&7DDoS-защита: " + (cg.isEnabled() ? "&aon" : "&cdisabled")));
        s.sendMessage(Msg.parse("&7  Чёрный список IP: &f" + cg.blacklistSize()));
        s.sendMessage(Msg.parse("&7  Временно блокировано IP: &f" + cg.blockedIpCount()));
        s.sendMessage(Msg.parse("&7OP-Guard: " + (og.isEnabled() ? "&aon" : "&cdisabled")));
        s.sendMessage(Msg.parse("&7  Allowed-ops: &f" + og.list().size()));
        s.sendMessage(Msg.parse("&7  Перехват /op: " + (og.isInterceptOpCmd() ? "&aon" : "&7off")));
    }

    private void doUnblock(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage(Msg.parse("&7/arisguard unblock <ip>")); return; }
        boolean ok = plugin.getConnectionGuard().unblock(args[1]);
        s.sendMessage(Msg.parse(ok ? "&aIP &e" + args[1] + " &aснят с временного бана."
                                   : "&7IP &e" + args[1] + " &7не был забанен."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String sub : SUBS) if (sub.startsWith(partial)) out.add(sub);
            return out;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("add"))) {
            return plugin.getOpGuard().list();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("unblock")) {
            return new ArrayList<>(plugin.getConnectionGuard().blockedIps());
        }
        return List.of();
    }
}
