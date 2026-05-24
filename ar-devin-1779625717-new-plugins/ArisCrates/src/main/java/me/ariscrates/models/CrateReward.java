package me.ariscrates.models;

import org.bukkit.inventory.ItemStack;

/**
 * Награда крейта.
 *   donateRankId != null  — донат-награда (выдаётся ранг)
 *   kitId       != null   — кит-награда (выдаётся кит из ArisDonate)
 *   иначе — обычный предмет.
 */
public record CrateReward(
        String display,
        ItemStack item,
        double chance,
        String rarity,
        String donateRankId,
        String kitId
) {
    public boolean isDonateReward() {
        return donateRankId != null && !donateRankId.isEmpty();
    }

    public boolean isKitReward() {
        return kitId != null && !kitId.isEmpty();
    }
}
