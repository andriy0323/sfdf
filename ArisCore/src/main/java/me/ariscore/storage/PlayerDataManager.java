package me.ariscore.storage;

import me.ariscore.ArisCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * One YAML file per player under plugins/ArisCore/players/&lt;uuid&gt;.yml.
 *
 * Stores everything: auth data, last login time, last logout time, balance, donate rank,
 * stats, played time, last seen, last position.
 */
public class PlayerDataManager {

    private final ArisCorePlugin plugin;
    private final File dir;

    public PlayerDataManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "players");
        if (!dir.exists()) dir.mkdirs();
    }

    public File file(UUID uuid) { return new File(dir, uuid + ".yml"); }
    public File file(String name) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        return file(p.getUniqueId());
    }

    public FileConfiguration load(UUID uuid) {
        return YamlConfiguration.loadConfiguration(file(uuid));
    }

    public FileConfiguration load(Player p) { return load(p.getUniqueId()); }

    public void save(UUID uuid, FileConfiguration cfg) {
        try { cfg.save(file(uuid)); } catch (IOException e) { plugin.getLogger().warning("save " + uuid + ": " + e); }
    }

    public void save(Player p, FileConfiguration cfg) { save(p.getUniqueId(), cfg); }

    public void saveAll() {
        // FileConfiguration is loaded per call, nothing held in memory to flush here.
    }
}
