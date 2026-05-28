package me.ariscore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {

    private ItemUtil() {}

    public static ItemStack build(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(Msg.mm(name).decoration(TextDecoration.ITALIC, false));
            if (lore != null && !lore.isEmpty()) {
                List<Component> components = new ArrayList<>();
                for (String l : lore) components.add(Msg.mm(l).decoration(TextDecoration.ITALIC, false));
                meta.lore(components);
            }
            meta.addItemFlags(ItemFlag.values());
            it.setItemMeta(meta);
        }
        return it;
    }

    public static ItemStack tagged(ItemStack it, org.bukkit.NamespacedKey key, String value) {
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key, PersistentDataType.STRING, value);
            it.setItemMeta(meta);
        }
        return it;
    }

    public static String getTag(ItemStack it, org.bukkit.NamespacedKey key) {
        if (it == null) return null;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
