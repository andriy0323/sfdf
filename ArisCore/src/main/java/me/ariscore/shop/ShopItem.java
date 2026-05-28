package me.ariscore.shop;

import org.bukkit.Material;

public final class ShopItem {
    private final String id;
    private final Material material;
    private final int amount;
    private final double price;
    private final int slot;
    private final String displayName;
    private final String startHex;
    private final String endHex;

    public ShopItem(String id, Material material, int amount, double price, int slot,
                    String displayName, String startHex, String endHex) {
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.slot = slot;
        this.displayName = displayName;
        this.startHex = startHex;
        this.endHex = endHex;
    }

    public String id()           { return id; }
    public Material material()   { return material; }
    public int amount()          { return amount; }
    public double price()        { return price; }
    public int slot()            { return slot; }
    public String displayName()  { return displayName; }
    public String startHex()     { return startHex; }
    public String endHex()       { return endHex; }
}
