package me.ariscore.crate;

import org.bukkit.Material;

public final class CrateReward {
    private final Material material;
    private final int amount;
    private final int weight;
    private final String label;
    private final String donate;

    public CrateReward(Material material, int amount, int weight, String label, String donate) {
        this.material = material;
        this.amount = amount;
        this.weight = weight;
        this.label = label;
        this.donate = donate;
    }

    public Material material() { return material; }
    public int amount()        { return amount; }
    public int weight()        { return weight; }
    public String label()      { return label; }
    public String donate()     { return donate; }
}
