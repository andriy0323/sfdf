package me.ariscore.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class Kit {
    private final String id;
    private final String displayName;
    private final String startHex;
    private final String endHex;
    private final Material icon;
    private final List<String> description;
    private final List<ItemStack> items;
    private final long cooldownMs;
    private final String donateRequired;
    private final boolean starter;
    private final int guiSlot;

    public Kit(String id, String displayName, String startHex, String endHex,
               Material icon, List<String> description, List<ItemStack> items,
               long cooldownMs, String donateRequired, boolean starter, int guiSlot) {
        this.id = id;
        this.displayName = displayName;
        this.startHex = startHex;
        this.endHex = endHex;
        this.icon = icon;
        this.description = description;
        this.items = items;
        this.cooldownMs = cooldownMs;
        this.donateRequired = donateRequired;
        this.starter = starter;
        this.guiSlot = guiSlot;
    }

    public String id()               { return id; }
    public String displayName()      { return displayName; }
    public String startHex()         { return startHex; }
    public String endHex()           { return endHex; }
    public Material icon()           { return icon; }
    public List<String> description(){ return description; }
    public List<ItemStack> items()   { return items; }
    public long cooldownMs()         { return cooldownMs; }
    public String donateRequired()   { return donateRequired; }
    public boolean starter()         { return starter; }
    public int guiSlot()             { return guiSlot; }
}
