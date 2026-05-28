package me.ariscore.scoreboard;

import me.ariscore.ArisCorePlugin;
import me.ariscore.donate.DonateRank;
import me.ariscore.util.Msg;
import me.ariscore.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sidebar scoreboard with: server name, rank, aris, playtime, deaths, kills.
 */
public class ScoreboardService {

    private final ArisCorePlugin plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private BukkitTask task;

    public ScoreboardService(ArisCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) task.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        boards.clear();
    }

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) update(p);
    }

    public void update(Player p) {
        Scoreboard sb = boards.computeIfAbsent(p.getUniqueId(), id -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective obj = sb.getObjective("aris-sidebar");
        if (obj == null) {
            obj = sb.registerNewObjective("aris-sidebar", Criteria.DUMMY,
                    Msg.gradient("ArisWorld", 0xFFD700, 0xFF6A00).decoration(TextDecoration.BOLD, true));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        DonateRank rank = plugin.donates().playerRank(p);
        String rankText = rank == null ? "<gray>Player</gray>" : rank.gradientName();
        int aris = (int) plugin.economy().balance(p);
        long pt = plugin.stats().playtime(p);
        int kills = plugin.stats().kills(p);
        int deaths = plugin.stats().deaths(p);

        // Build the sidebar lines from bottom-up (Bukkit shows higher scores at top).
        Map<String, Integer> lines = new java.util.LinkedHashMap<>();
        lines.put("<dark_gray>━━━━━━━━━━━━━━",            12);
        lines.put("<gray>Player: <white>" + p.getName(), 11);
        lines.put("<gray>Rank: " + rankText,             10);
        lines.put("<gray>Aris: <yellow>" + aris,          9);
        lines.put("<gray>Kills: <green>" + kills,         8);
        lines.put("<gray>Deaths: <red>" + deaths,         7);
        lines.put("<gray>Played: <aqua>" + TimeUtil.formatLeft(pt * 1000),6);
        lines.put("<dark_gray>━━━━━━━━━━━━━━ ",           5);
        lines.put("<gray>Online: <green>" + Bukkit.getOnlinePlayers().size(),    4);
        lines.put("<gray>aris.world",                     3);

        // Bukkit's Score API still uses legacy entry strings. Use teams to render arbitrary components per slot.
        for (String entry : new java.util.HashSet<>(sb.getEntries())) sb.resetScores(entry);
        for (Team t : new java.util.HashSet<>(sb.getTeams())) t.unregister();

        int idx = 0;
        for (var e : lines.entrySet()) {
            idx++;
            String entry = "§" + Integer.toHexString(idx % 16) + " "; // unique invisible entry per line
            // We instead use unique invisible chars per row.
            entry = invisibleEntry(idx);
            Team t = sb.registerNewTeam("aris-line-" + idx);
            t.addEntry(entry);
            t.prefix(Msg.mm(e.getKey()));
            obj.getScore(entry).setScore(e.getValue());
        }
        p.setScoreboard(sb);
    }

    private static String invisibleEntry(int idx) {
        // ChatColor codes are 2 chars (§ + char). 14 unique pairs is enough for our lines.
        char c = (char) ('a' + (idx % 16));
        return "§" + c + "§r";
    }
}
