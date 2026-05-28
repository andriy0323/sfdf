package me.ariscore.command;

import me.ariscore.ArisCorePlugin;
import me.ariscore.command.impl.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import java.util.function.Function;

public final class CommandRegistry {

    private CommandRegistry() {}

    public static void registerAll(ArisCorePlugin plugin) {
        bind(plugin, "register",   new RegisterCommand(plugin));
        bind(plugin, "login",      new LoginCommand(plugin));
        bind(plugin, "changepass", new ChangePassCommand(plugin));
        bind(plugin, "oper",       new OperCommand(plugin));

        bind(plugin, "aris",       new ArisCommand(plugin));
        bind(plugin, "a",          new ArisCommand(plugin));

        bind(plugin, "spawn",      new SpawnCommand(plugin));
        bind(plugin, "setspawn",   new SetSpawnCommand(plugin));
        bind(plugin, "tpa",        new TpaCommand(plugin));
        bind(plugin, "tpaccept",   new TpAcceptCommand(plugin));
        bind(plugin, "tpadeny",    new TpDenyCommand(plugin));
        bind(plugin, "setwarp",    new SetWarpCommand(plugin));
        bind(plugin, "warp",       new WarpCommand(plugin));
        bind(plugin, "delwarp",    new DelWarpCommand(plugin));
        bind(plugin, "warps",      new WarpsCommand(plugin));

        bind(plugin, "shop",       new ShopCommand(plugin));

        bind(plugin, "kits",       new KitsCommand(plugin));
        bind(plugin, "kit",        new KitCommand(plugin));
        bind(plugin, "free",       new FreeCommand(plugin));

        bind(plugin, "donate",     new DonateCommand(plugin));
        bind(plugin, "don",        new DonateCommand(plugin));
        bind(plugin, "arisdonate", new ArisDonateAdminCommand(plugin));
        bind(plugin, "ad",         new ArisDonateAdminCommand(plugin));

        bind(plugin, "rg",         new RgCommand(plugin));
        bind(plugin, "region",     new RgCommand(plugin));
        bind(plugin, "regionblock",new RegionBlockCommand(plugin));

        bind(plugin, "dc",         new DcCommand(plugin));

        bind(plugin, "speed",      new SpeedCommand(plugin));
        bind(plugin, "v",          new VanishCommand(plugin));
        bind(plugin, "vanish",     new VanishCommand(plugin));
        bind(plugin, "god",        new GodCommand(plugin));
        bind(plugin, "fly",        new FlyCommand(plugin));
        bind(plugin, "heal",       new HealCommand(plugin));
        bind(plugin, "feed",       new FeedCommand(plugin));
        bind(plugin, "gmc",        new GamemodeCommand(plugin, "creative"));
        bind(plugin, "gms",        new GamemodeCommand(plugin, "survival"));
        bind(plugin, "gma",        new GamemodeCommand(plugin, "adventure"));
        bind(plugin, "gmsp",       new GamemodeCommand(plugin, "spectator"));
        bind(plugin, "gm",         new GamemodeCommand(plugin, null));
        bind(plugin, "ariscorereload", new ReloadCommand(plugin));
    }

    private static void bind(ArisCorePlugin plugin, String name, CommandExecutor exec) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd == null) {
            plugin.getLogger().warning("Command not declared in plugin.yml: /" + name);
            return;
        }
        cmd.setExecutor(exec);
        if (exec instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
    }
}
