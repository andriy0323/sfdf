package me.arisguard.managers;

import me.arisguard.ArisGuardPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Защита от flood-подключений и DDoS-попыток на уровне приложения.
 *
 * Логика:
 *   1. Постоянный чёрный список IP (из config.yml) — мгновенный отказ.
 *   2. Сколько раз с одного IP пытались подключиться за окно window-seconds:
 *      если больше max-attempts — IP попадает в temp-ban на block-seconds.
 *   3. Сколько онлайн-игроков с одного IP одновременно: если уже
 *      max-concurrent-per-ip — новые подключения отклоняются до выхода
 *      существующих.
 *
 * Сетевой DDoS (SYN-flood и т.п.) этим плагином не закрывается — для этого
 * нужны хардварный firewall / прокси. Но прикладные flood-атаки (например
 * "зайди-выйди" ботнетом) этот модуль успешно обрезает.
 */
public class ConnectionGuard {

    private final ArisGuardPlugin plugin;

    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> tempBlocked = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> concurrent = new ConcurrentHashMap<>();
    private final Set<String> ipBlacklist = ConcurrentHashMap.newKeySet();

    private boolean enabled;
    private int maxAttempts;
    private long windowMillis;
    private long blockMillis;
    private int maxConcurrent;
    private String kickMessage;
    private String blacklistMessage;

    public ConnectionGuard(ArisGuardPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        var cfg = plugin.getConfig();
        this.enabled          = cfg.getBoolean("ddos.enabled", true);
        this.maxAttempts      = Math.max(1, cfg.getInt("ddos.max-attempts", 5));
        this.windowMillis     = Math.max(1L, cfg.getLong("ddos.window-seconds", 10L)) * 1000L;
        this.blockMillis      = Math.max(1L, cfg.getLong("ddos.block-seconds", 60L)) * 1000L;
        this.maxConcurrent    = Math.max(1, cfg.getInt("ddos.max-concurrent-per-ip", 3));
        this.kickMessage      = cfg.getString("ddos.kick-message", "&cСлишком много подключений. Попробуйте позже.");
        this.blacklistMessage = cfg.getString("ddos.blacklist-message", "&cВаш IP в чёрном списке.");

        ipBlacklist.clear();
        for (String ip : cfg.getStringList("ddos.ip-blacklist")) {
            if (ip != null && !ip.isBlank()) ipBlacklist.add(ip.trim());
        }
    }

    public boolean isEnabled() { return enabled; }

    /** Решение по входящему подключению. null = пропустить, иначе причина отказа. */
    public String evaluate(String ip) {
        if (!enabled || ip == null || ip.isBlank()) return null;

        if (ipBlacklist.contains(ip)) return blacklistMessage;

        long now = System.currentTimeMillis();

        Long until = tempBlocked.get(ip);
        if (until != null) {
            if (until > now) return kickMessage;
            tempBlocked.remove(ip);
        }

        AtomicInteger c = concurrent.get(ip);
        if (c != null && c.get() >= maxConcurrent) return kickMessage;

        Deque<Long> dq = attempts.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && now - dq.peekFirst() > windowMillis) dq.pollFirst();
            dq.addLast(now);
            if (dq.size() > maxAttempts) {
                tempBlocked.put(ip, now + blockMillis);
                plugin.getLogger().warning("[ArisGuard] DDoS: IP " + ip + " превысил лимит ("
                        + dq.size() + " попыток за " + (windowMillis / 1000) + "s), временно блокирован на "
                        + (blockMillis / 1000) + "s.");
                return kickMessage;
            }
        }
        return null;
    }

    /** Игрок успешно зашёл с IP — увеличить счётчик онлайн. */
    public void onJoin(Player p) {
        if (p == null || p.getAddress() == null) return;
        String ip = p.getAddress().getAddress().getHostAddress();
        concurrent.computeIfAbsent(ip, k -> new AtomicInteger()).incrementAndGet();
    }

    /** Игрок вышел — уменьшить счётчик онлайн. */
    public void onQuit(Player p) {
        if (p == null || p.getAddress() == null) return;
        String ip = p.getAddress().getAddress().getHostAddress();
        AtomicInteger c = concurrent.get(ip);
        if (c != null && c.decrementAndGet() <= 0) concurrent.remove(ip);
    }

    /** Снять временный бан IP. true если был снят. */
    public boolean unblock(String ip) {
        if (ip == null) return false;
        boolean removed = tempBlocked.remove(ip) != null;
        attempts.remove(ip);
        return removed;
    }

    public int blockedIpCount() { return tempBlocked.size(); }
    public int blacklistSize()  { return ipBlacklist.size(); }

    public java.util.Set<String> blockedIps() {
        return java.util.Set.copyOf(tempBlocked.keySet());
    }
}
