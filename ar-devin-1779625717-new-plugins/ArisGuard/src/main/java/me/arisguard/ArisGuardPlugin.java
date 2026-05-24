package me.arisguard;

import me.arisguard.commands.ArisGuardCommand;
import me.arisguard.listeners.OpListener;
import me.arisguard.listeners.PreLoginListener;
import me.arisguard.managers.ConnectionGuard;
import me.arisguard.managers.OpGuard;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * ArisGuard — защита от прикладного DDoS / flood-конектов и от
 * несанкционированного OP-доступа. Никнейм администратора держится в
 * config.yml: op-guard.allowed-ops. По умолчанию там AndryshaOff —
 * добавить других можно командой /arisguard add <ник>.
 */
public class ArisGuardPlugin extends JavaPlugin {

    private ConnectionGuard connectionGuard;
    private OpGuard opGuard;
    private BukkitTask opScanTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.connectionGuard = new ConnectionGuard(this);
        this.opGuard         = new OpGuard(this);

        getServer().getPluginManager().registerEvents(new PreLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new OpListener(this), this);

        PluginCommand cmd = getCommand("arisguard");
        if (cmd != null) {
            ArisGuardCommand handler = new ArisGuardCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        opGuard.scanAndEnforce();
        restartOpScan();

        getLogger().info("ArisGuard v" + getDescription().getVersion() + " включён. "
                + "Allowed-ops: " + opGuard.list().size()
                + ", DDoS: " + (connectionGuard.isEnabled() ? "on" : "off")
                + ", OP-Guard: " + (opGuard.isEnabled() ? "on" : "off") + ".");
    }

    @Override
    public void onDisable() {
        if (opScanTask != null) {
            opScanTask.cancel();
            opScanTask = null;
        }
    }

    public void restartOpScan() {
        if (opScanTask != null) {
            opScanTask.cancel();
            opScanTask = null;
        }
        long ticks = opGuard.getScanIntervalTicks();
        if (ticks <= 0) return;
        opScanTask = getServer().getScheduler().runTaskTimer(this, () -> opGuard.scanAndEnforce(), ticks, ticks);
    }

    public ConnectionGuard getConnectionGuard() { return connectionGuard; }
    public OpGuard getOpGuard()                  { return opGuard; }
}
