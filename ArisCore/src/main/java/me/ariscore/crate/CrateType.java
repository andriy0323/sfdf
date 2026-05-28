package me.ariscore.crate;

import org.bukkit.Material;

import java.util.List;

public final class CrateType {
    private final String id;
    private final String displayName;
    private final String startHex;
    private final String endHex;
    private final Material icon;
    private final double price;
    private final String donateRequired;
    private final List<CrateReward> rewards;

    public CrateType(String id, String displayName, String startHex, String endHex,
                     Material icon, double price, String donateRequired, List<CrateReward> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.startHex = startHex;
        this.endHex = endHex;
        this.icon = icon;
        this.price = price;
        this.donateRequired = donateRequired;
        this.rewards = rewards;
    }

    public String id()            { return id; }
    public String displayName()   { return displayName; }
    public String startHex()      { return startHex; }
    public String endHex()        { return endHex; }
    public Material icon()        { return icon; }
    public double price()         { return price; }
    public String donateRequired(){ return donateRequired; }
    public List<CrateReward> rewards() { return rewards; }
}
