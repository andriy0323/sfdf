package me.arisdonate.listeners;

import me.arisdonate.ArisDonatePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация кастомной чары "Телепатия": если в правой руке инструмент с
 * PDC-меткой key_telekinesis, дроп с блока попадает напрямую в инвентарь
 * игрока, а опыт выдаётся сразу. Что не помещается — падает в исходную
 * точку (как раньше).
 */
public class TelekinesisListener implements Listener {

    private final ArisDonatePlugin plugin;

    public TelekinesisListener(ArisDonatePlugin plugin) { this.plugin = plugin; }

    private boolean hasTelekinesis(ItemStack item) {
        if (item == null) return false;
        ItemMeta im = item.getItemMeta();
        if (im == null) return false;
        Byte b = im.getPersistentDataContainer().get(plugin.keyTelekinesis(), PersistentDataType.BYTE);
        return b != null && b == (byte) 1;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!hasTelekinesis(tool)) return;
        // Сразу заберём опыт
        int xp = e.getExpToDrop();
        if (xp > 0) {
            p.giveExp(xp);
            e.setExpToDrop(0);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!hasTelekinesis(tool)) return;
        Location loc = e.getBlock().getLocation();
        World world = loc.getWorld();

        var iter = e.getItems().iterator();
        while (iter.hasNext()) {
            var drop = iter.next();
            ItemStack stack = drop.getItemStack();
            Map<Integer, ItemStack> leftover = p.getInventory().addItem(stack);
            if (leftover.isEmpty()) {
                iter.remove();
                drop.remove();
            } else {
                // часть поместилась — что осталось, оставим в дропе
                int taken = stack.getAmount();
                int leftoverCount = 0;
                for (ItemStack left : leftover.values()) leftoverCount += left.getAmount();
                if (leftoverCount < taken) {
                    drop.getItemStack().setAmount(leftoverCount);
                }
            }
        }
    }
}
