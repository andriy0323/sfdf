package me.ariscombat;

import me.ariscombat.commands.CombatCommand;
import me.ariscombat.listeners.CombatLogoutListener;
import me.ariscombat.listeners.CombatRestrictionsListener;
import me.ariscombat.listeners.CombatTagListener;
import me.ariscombat.managers.CombatManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisCombatPlugin extends JavaPlugin {

    private CombatManager combatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        combatManager = new CombatManager(this);

        getServer().getPluginManager().registerEvents(new CombatTagListener(this),          this);
        getServer().getPluginManager().registerEvents(new CombatRestrictionsListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatLogoutListener(this),       this);

        CombatCommand cmd = new CombatCommand(this);
        if (getCommand("ct") != null) {
            getCommand("ct").setExecutor(cmd);
            getCommand("ct").setTabCompleter(cmd);
        }

        getLogger().info("ArisCombat v1.0.0 включён. КТ = "
                + combatManager.durationSeconds() + " сек.");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) combatManager.shutdown();
        getLogger().info("ArisCombat выключен.");
    }

    public CombatManager getCombatManager() { return combatManager; }
}
