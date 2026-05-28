package me.ariscore.donate;

import org.bukkit.Material;

import java.util.List;

/** Single immutable donate (or staff) rank. */
public final class DonateRank {
    private final String id;
    private final String displayName;
    private final String startHex;
    private final String endHex;
    private final int weight;
    private final int regionLimit;
    private final int homeLimit;
    private final Material shulker;
    private final List<String> description;
    private final List<String> permissions;
    private final List<String> commands;
    private final int guiSlot;
    private final String kitId;
    private final boolean staff;

    public DonateRank(String id, String displayName, String startHex, String endHex,
                      int weight, int regionLimit, int homeLimit,
                      Material shulker, List<String> description,
                      List<String> permissions, List<String> commands,
                      int guiSlot, String kitId, boolean staff) {
        this.id = id;
        this.displayName = displayName;
        this.startHex = startHex;
        this.endHex = endHex;
        this.weight = weight;
        this.regionLimit = regionLimit;
        this.homeLimit = homeLimit;
        this.shulker = shulker;
        this.description = description;
        this.permissions = permissions;
        this.commands = commands;
        this.guiSlot = guiSlot;
        this.kitId = kitId;
        this.staff = staff;
    }

    public String id()            { return id; }
    public String displayName()   { return displayName; }
    public String startHex()      { return startHex; }
    public String endHex()        { return endHex; }
    public int weight()           { return weight; }
    public int regionLimit()      { return regionLimit; }
    public int homeLimit()        { return homeLimit; }
    public Material shulker()     { return shulker; }
    public List<String> description() { return description; }
    public List<String> permissions() { return permissions; }
    public List<String> commands()    { return commands; }
    public int guiSlot()              { return guiSlot; }
    public String kitId()             { return kitId; }
    public boolean staff()            { return staff; }

    public String gradientName() {
        return "<grad:#" + startHex + ":#" + endHex + "><b>" + displayName + "</b></grad>";
    }
}
