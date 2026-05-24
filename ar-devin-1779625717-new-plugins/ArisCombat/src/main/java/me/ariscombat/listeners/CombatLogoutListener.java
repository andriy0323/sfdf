package me.ariscombat.listeners;

import me.ariscombat.ArisCombatPlugin;
import me.ariscombat.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Combat log → смерть. Если игрок вышел во время КТ:
 *  - убиваем его (setHealth(0)); при следующем входе он респавнится мёртвым
 *  - опционально дропаем его инвентарь и опыт на месте выхода
 *  - анонсируем
 */
public class CombatLogoutListener implements Listener {

    private final ArisCombatPlugin plugin;

    public CombatLogoutListener(ArisCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getCombatManager().inCombat(p)) return;
        if (p.hasPermission("ariscombat.bypass")) return;
        if (!plugin.getConfig().getBoolean("combat.kill-on-disconnect", true)) {
            plugin.getCombatManager().clear(p);
            return;
        }

        // Дропаем инвентарь/опыт перед смертью (vanilla death сделает это, но мы кладём в мир сразу,
        // потому что setHealth(0) при оффлайн-игроке не всегда триггерит PlayerDeathEvent.dropItems).
        boolean drop = plugin.getConfig().getBoolean("combat.drop-inventory-on-disconnect", true);
        if (drop) {
            for (ItemStack it : p.getInventory().getContents()) {
                if (it != null && !it.getType().isAir()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), it);
                }
            }
            for (ItemStack it : p.getInventory().getArmorContents()) {
                if (it != null && !it.getType().isAir()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), it);
                }
            }
            ItemStack off = p.getInventory().getItemInOffHand();
            if (off != null && !off.getType().isAir()) {
                p.getWorld().dropItemNaturally(p.getLocation(), off);
            }
            p.getInventory().clear();
            // Бросаем опыт
            int xp = p.getTotalExperience();
            if (xp > 0) {
                p.getWorld().spawn(p.getLocation(), org.bukkit.entity.ExperienceOrb.class,
                        orb -> orb.setExperience(Math.min(xp, 100)));
                p.setTotalExperience(0);
                p.setLevel(0);
                p.setExp(0f);
            }
        }

        // Убиваем (на след. входе будет screen смерти / респавна).
        p.setHealth(0.0);

        String bc = plugin.getConfig().getString("combat.on-disconnect-broadcast",
                "&c⚔ &e%player% &cвышел во время КТ — позор!");
        if (bc != null && !bc.isEmpty()) {
            Bukkit.broadcast(Msg.parse(bc.replace("%player%", p.getName())));
        }
        plugin.getCombatManager().clear(p);
    }
}
