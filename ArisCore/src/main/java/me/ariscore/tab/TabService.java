package me.ariscore.tab;

import me.ariscore.ArisCorePlugin;
import me.ariscore.donate.DonateRank;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * TAB header/footer + per-player display name.
 *
 *   Server name "ArisWorld"
 *   Online "X" (not "X/20")
 *   In TAB: donate prefix (bold, RGB) | Player (white) | Rank (gradient, bold)
 */
public class TabService {

    private final ArisCorePlugin plugin;
    private BukkitTask task;

    public TabService(ArisCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        int period = Math.max(20, plugin.getConfig().getInt("tab.refresh-ticks", 40));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, period);
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
    }

    private void tick() {
        int online = Bukkit.getOnlinePlayers().size();
        Component header = buildHeader(online);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Component footer = buildFooter(p);
            p.sendPlayerListHeaderAndFooter(header, footer);
            DonateRank rank = plugin.donates().playerRank(p);
            Component name;
            if (rank != null) {
                int from = Msg.parseHex(rank.startHex(), 0xFFFFFF);
                int to   = Msg.parseHex(rank.endHex(), 0xFFFFFF);
                name = Msg.gradient(rank.displayName(), from, to).decoration(TextDecoration.BOLD, true)
                        .append(Component.text(" ", NamedTextColor.GRAY))
                        .append(Component.text(p.getName(), NamedTextColor.WHITE).decoration(TextDecoration.BOLD, true));
            } else {
                name = Component.text(p.getName(), NamedTextColor.WHITE).decoration(TextDecoration.BOLD, true);
            }
            p.playerListName(name);
        }
    }

    private Component buildHeader(int online) {
        return Component.empty()
                .append(Component.newline())
                .append(Component.text("  "))
                .append(Msg.gradient("ArisWorld", 0xFFD700, 0xFF6A00).decoration(TextDecoration.BOLD, true))
                .append(Component.text("  "))
                .append(Component.newline())
                .append(Component.text("  "))
                .append(Component.text("Online: ", NamedTextColor.GRAY))
                .append(Component.text(online, NamedTextColor.GREEN))
                .append(Component.text("  "))
                .append(Component.newline());
    }

    private Component buildFooter(Player viewer) {
        long playtime = plugin.stats().playtime(viewer);
        int aris = (int) plugin.economy().balance(viewer);
        long pingMs = viewer.getPing();
        TextColor pingColor = pingMs < 80 ? NamedTextColor.GREEN : pingMs < 150 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        return Component.empty()
                .append(Component.newline())
                .append(Component.text("  Aris: ", NamedTextColor.GRAY))
                .append(Component.text(aris, NamedTextColor.YELLOW))
                .append(Component.text("   Ping: ", NamedTextColor.GRAY))
                .append(Component.text(pingMs + " ms", pingColor))
                .append(Component.newline())
                .append(Msg.gradient("aris.world", 0xFFD700, 0xFF6A00))
                .append(Component.newline());
    }
}
