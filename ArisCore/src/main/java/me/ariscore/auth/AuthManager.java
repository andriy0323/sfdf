package me.ariscore.auth;

import me.ariscore.ArisCorePlugin;
import me.ariscore.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Register / login with a 5-minute session: if the player rejoins within 5 minutes
 * of their previous quit, /login is skipped (the session is still valid). Otherwise
 * the player must /login (or /register if they have never registered).
 */
public class AuthManager implements Listener {

    private static final long SESSION_MS = 5L * 60L * 1000L;
    private static final Set<String> ALLOWED_COMMANDS = Set.of("/login", "/l", "/register", "/reg");

    private final ArisCorePlugin plugin;
    private final Set<UUID> unauthorized = new HashSet<>();

    public AuthManager(ArisCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAuthorized(Player p) { return !unauthorized.contains(p.getUniqueId()); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration cfg = plugin.data().load(p);

        String hash = cfg.getString("auth.password");
        long lastQuit = cfg.getLong("auth.last-quit", 0L);
        boolean sessionAlive = hash != null && (System.currentTimeMillis() - lastQuit) < SESSION_MS;

        if (sessionAlive) {
            unauthorized.remove(p.getUniqueId());
            p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Session restored. Welcome back, " + p.getName() + "!</grad>")));
            return;
        }

        unauthorized.add(p.getUniqueId());
        if (hash == null) {
            sendBigBanner(p,
                    "<grad:#FFD700:#FF6A00>Welcome to ArisWorld!</grad>",
                    "<gray>You are not registered. Please use:",
                    "<click:suggest:/register password password><grad:#7CFC00:#32CD32>/register &lt;password&gt; &lt;password&gt;</grad></click>");
        } else {
            sendBigBanner(p,
                    "<grad:#FFD700:#FF6A00>Welcome back!</grad>",
                    "<gray>Please log in:",
                    "<click:suggest:/login password><grad:#7CFC00:#32CD32>/login &lt;password&gt;</grad></click>");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (isAuthorized(p)) {
            FileConfiguration cfg = plugin.data().load(p);
            cfg.set("auth.last-quit", System.currentTimeMillis());
            plugin.data().save(p, cfg);
        }
        unauthorized.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (isAuthorized(p)) return;
        String first = e.getMessage().split(" ")[0].toLowerCase();
        if (!ALLOWED_COMMANDS.contains(first)) {
            e.setCancelled(true);
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>You must /login or /register first.</red>")));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!isAuthorized(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Msg.prefix().append(Msg.mm("<red>Log in before chatting.</red>")));
        }
    }

    @EventHandler(ignoreCancelled = true) public void onMove(PlayerMoveEvent e) {
        if (!isAuthorized(e.getPlayer())) {
            if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
                e.setTo(e.getFrom());
            }
        }
    }
    @EventHandler(ignoreCancelled = true) public void onBreak(BlockBreakEvent e) { if (!isAuthorized(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onPlace(BlockPlaceEvent e) { if (!isAuthorized(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onInteract(PlayerInteractEvent e) { if (!isAuthorized(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onClick(InventoryClickEvent e) { if (e.getWhoClicked() instanceof Player p && !isAuthorized(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageEvent e) { if (e.getEntity() instanceof Player p && !isAuthorized(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onPvp(EntityDamageByEntityEvent e) { if (e.getDamager() instanceof Player p && !isAuthorized(p)) e.setCancelled(true); }

    public boolean register(Player p, String pw, String confirm) {
        FileConfiguration cfg = plugin.data().load(p);
        if (cfg.getString("auth.password") != null) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>You are already registered. Use /login.</red>")));
            return false;
        }
        if (!pw.equals(confirm)) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Passwords do not match.</red>")));
            return false;
        }
        if (pw.length() < 4) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Password too short (min 4 chars).</red>")));
            return false;
        }
        String hash = BCrypt.hashpw(pw, BCrypt.gensalt(8));
        cfg.set("auth.password", hash);
        cfg.set("auth.first-join", System.currentTimeMillis());
        plugin.data().save(p, cfg);
        unauthorized.remove(p.getUniqueId());
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Registered. Welcome to ArisWorld!</grad>")));
        plugin.teleport().teleportToSpawn(p);
        plugin.kits().tryGiveStarter(p, true);
        return true;
    }

    public boolean login(Player p, String pw) {
        FileConfiguration cfg = plugin.data().load(p);
        String hash = cfg.getString("auth.password");
        if (hash == null) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>You are not registered. Use /register.</red>")));
            return false;
        }
        if (!BCrypt.checkpw(pw, hash)) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Wrong password.</red>")));
            return false;
        }
        unauthorized.remove(p.getUniqueId());
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Logged in.</grad>")));
        return true;
    }

    public boolean changePassword(Player p, String oldPw, String newPw) {
        FileConfiguration cfg = plugin.data().load(p);
        String hash = cfg.getString("auth.password");
        if (hash == null || !BCrypt.checkpw(oldPw, hash)) {
            p.sendMessage(Msg.prefix().append(Msg.mm("<red>Wrong current password.</red>")));
            return false;
        }
        cfg.set("auth.password", BCrypt.hashpw(newPw, BCrypt.gensalt(8)));
        plugin.data().save(p, cfg);
        p.sendMessage(Msg.prefix().append(Msg.mm("<grad:#7CFC00:#32CD32>Password updated.</grad>")));
        return true;
    }

    private void sendBigBanner(Player p, String title, String... lines) {
        p.sendMessage(Component.empty());
        p.sendMessage(Msg.mm("<grad:#FFD700:#FF4500>━━━━━━━━━━━━━━━━━━━━━━━━━</grad>"));
        p.sendMessage(Msg.mm(title));
        for (String l : lines) p.sendMessage(Msg.mm(l));
        p.sendMessage(Msg.mm("<grad:#FFD700:#FF4500>━━━━━━━━━━━━━━━━━━━━━━━━━</grad>"));
        p.sendMessage(Component.empty());
    }
}
