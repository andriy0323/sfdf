package me.ariscore.economy;

import me.ariscore.ArisCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

/** "aris" currency. One double per player, persisted via PlayerDataManager. */
public class EconomyManager {

    private final ArisCorePlugin plugin;

    public EconomyManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
    }

    public double balance(UUID id) {
        return plugin.data().load(id).getDouble("aris", 0.0);
    }

    public double balance(Player p) { return balance(p.getUniqueId()); }

    public double balance(String name) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        return balance(op.getUniqueId());
    }

    public void set(UUID id, double amount) {
        FileConfiguration cfg = plugin.data().load(id);
        cfg.set("aris", Math.max(0, amount));
        plugin.data().save(id, cfg);
    }

    public void give(UUID id, double amount) {
        set(id, balance(id) + amount);
    }

    public boolean take(UUID id, double amount) {
        double cur = balance(id);
        if (cur < amount) return false;
        set(id, cur - amount);
        return true;
    }

    public void reset(UUID id) { set(id, 0); }

    public void giveAllOnline(double amount) {
        for (Player p : Bukkit.getOnlinePlayers()) give(p.getUniqueId(), amount);
    }
}
