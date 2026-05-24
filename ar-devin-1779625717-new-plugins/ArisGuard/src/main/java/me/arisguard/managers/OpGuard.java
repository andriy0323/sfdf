package me.arisguard.managers;

import me.arisguard.ArisGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Защита от несанкционированного OP.
 *
 * Принцип: список allowed-ops в config.yml — единственный источник истины.
 * Любой игрок, у которого в данный момент стоит флаг OP (включая offline,
 * через ops.json), но чей ник не входит в allowed-ops, будет автоматически
 * разоп-нут:
 *   - при заходе (PlayerJoinEvent),
 *   - периодически (scan-interval-seconds),
 *   - сразу после попытки выполнить /op (см. {@code interceptOpCommand}).
 *
 * Имена сравниваются регистронезависимо.
 */
public class OpGuard {

    private final ArisGuardPlugin plugin;
    private final Set<String> allowedLower = Collections.synchronizedSet(new LinkedHashSet<>());

    private boolean enabled;
    private boolean intercept;
    private boolean alert;
    private long scanIntervalTicks;

    public OpGuard(ArisGuardPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        var cfg = plugin.getConfig();
        this.enabled    = cfg.getBoolean("op-guard.enabled", true);
        this.intercept  = cfg.getBoolean("op-guard.intercept-op-command", true);
        this.alert      = cfg.getBoolean("op-guard.alert-on-detection", true);
        long sec        = Math.max(0L, cfg.getLong("op-guard.scan-interval-seconds", 5L));
        this.scanIntervalTicks = sec * 20L;

        synchronized (allowedLower) {
            allowedLower.clear();
            for (String name : cfg.getStringList("op-guard.allowed-ops")) {
                if (name != null && !name.isBlank()) allowedLower.add(name.trim().toLowerCase(Locale.ROOT));
            }
        }
    }

    public boolean isEnabled()         { return enabled; }
    public boolean isInterceptOpCmd()  { return intercept; }
    public long getScanIntervalTicks() { return scanIntervalTicks; }

    public boolean isAllowed(String name) {
        if (name == null) return false;
        return allowedLower.contains(name.toLowerCase(Locale.ROOT));
    }

    /** Добавить ник. true если действительно добавлен. Сохраняет config. */
    public boolean add(String name) {
        if (name == null || name.isBlank()) return false;
        String norm = name.trim();
        boolean added;
        synchronized (allowedLower) {
            added = allowedLower.add(norm.toLowerCase(Locale.ROOT));
        }
        if (added) persistAllowedFromMemory(norm);
        return added;
    }

    /** Удалить ник. true если был. Сохраняет config. */
    public boolean remove(String name) {
        if (name == null || name.isBlank()) return false;
        boolean removed;
        synchronized (allowedLower) {
            removed = allowedLower.remove(name.trim().toLowerCase(Locale.ROOT));
        }
        if (removed) persistAllowedAfterRemoval(name.trim());
        return removed;
    }

    public List<String> list() {
        var cfg = plugin.getConfig();
        List<String> raw = cfg.getStringList("op-guard.allowed-ops");
        if (raw.isEmpty()) {
            synchronized (allowedLower) {
                return new ArrayList<>(allowedLower);
            }
        }
        return new ArrayList<>(raw);
    }

    /** Проход по всем OP-сущностям сервера и разоп-нуть тех, кого нет в allowed-list. */
    public int scanAndEnforce() {
        if (!enabled) return 0;
        int deopped = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp() && !isAllowed(p.getName())) {
                p.setOp(false);
                deopped++;
                if (alert) plugin.getLogger().warning("[ArisGuard] OP-Guard: online-игрок " + p.getName()
                        + " не в allowed-ops — снят OP.");
            }
        }

        for (OfflinePlayer op : Bukkit.getOperators()) {
            String name = op.getName();
            if (name == null) continue;
            if (!isAllowed(name)) {
                op.setOp(false);
                deopped++;
                if (alert) plugin.getLogger().warning("[ArisGuard] OP-Guard: offline-OP " + name
                        + " не в allowed-ops — снят OP.");
            }
        }
        return deopped;
    }

    private void persistAllowedFromMemory(String original) {
        var cfg = plugin.getConfig();
        List<String> current = new ArrayList<>(cfg.getStringList("op-guard.allowed-ops"));
        boolean exists = current.stream().anyMatch(s -> s.equalsIgnoreCase(original));
        if (!exists) current.add(original);
        cfg.set("op-guard.allowed-ops", current);
        plugin.saveConfig();
    }

    private void persistAllowedAfterRemoval(String original) {
        var cfg = plugin.getConfig();
        List<String> current = new ArrayList<>(cfg.getStringList("op-guard.allowed-ops"));
        current.removeIf(s -> s.equalsIgnoreCase(original));
        cfg.set("op-guard.allowed-ops", current);
        plugin.saveConfig();
    }
}
