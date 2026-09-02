package rickiewars.guishop.shop;

import rickiewars.guishop.api.minecraft.IItemStack;

/**
 * Computes a payout for selling an item, lowering the listing's {@code sellPrice}
 * for wear, repairs, and cosmetic changes relative to the originally listed item.
 * <p>
 * {@code sellPrice == 0} always pays out {@code 0}. Otherwise:
 * <pre>{@code
 * adjustedPayout = max(round(sellPrice * multiplier), 1)
 * multiplier     = max(damageMultiplier * repairMultiplier * nameMultiplier * loreMultiplier, minValueFraction)
 * }</pre>
 * {@code nameMultiplier} and {@code loreMultiplier} each penalize one cosmetic change made since
 * listing:
 * <ul>
 *     <li>{@code nameMultiplier} = {@code 1 - customNamePenalty} if the custom name was changed,
 *     else {@code 1}</li>
 *     <li>{@code loreMultiplier} = {@code 1 - lorePenalty} if the lore was changed, else
 *     {@code 1}</li>
 * </ul>
 *
 * @param firstUsePenalty           fraction (0-1) knocked off {@code damageMultiplier} the instant
 *                                  an item takes its first point of damage
 * @param minValueFraction          floor (0-1) below which no multiplier can drop, however worn,
 *                                  repaired, or renamed the item is
 * @param damageCurveExponent       exponent (&gt;0) shaping the durability payout curve; 1 = linear,
 *                                  &lt;1 = drops fast early then flattens (like a new car losing its
 *                                  value the moment you drive off from the dealership),
 *                                  &gt;1 = stays high early then drops off faster near the
 *                                  end of its durability
 * @param repairCostPenaltyPerPoint fraction (0-1) knocked off {@code repairMultiplier} per point
 *                                  the item's repair cost has risen since listing
 * @param customNamePenalty         fraction (0-1) knocked off {@code nameMultiplier} if the custom
 *                                  name was changed
 * @param lorePenalty               fraction (0-1) knocked off {@code loreMultiplier} if the lore
 *                                  was changed
 */
public record SellPricing(
        double firstUsePenalty,
        double minValueFraction,
        double damageCurveExponent,
        double repairCostPenaltyPerPoint,
        double customNamePenalty,
        double lorePenalty
) {
    public static final SellPricing DEFAULT = new SellPricing(0.15, 0.05, 3.0, 0.02, 0.10, 0.10);

    public long adjustedPayout(ShopItem listing, IItemStack playerStack) {
        if (listing.sellPrice() == 0) return 0;

        double damageMultiplier = damageMultiplier(listing.stack(), playerStack);
        double repairMultiplier = repairMultiplier(listing.stack(), playerStack);
        double nameMultiplier = listing.stack().componentDiffers(IItemStack.ComponentKey.CUSTOM_NAME, playerStack) ? 1 - customNamePenalty : 1.0;
        double loreMultiplier = listing.stack().componentDiffers(IItemStack.ComponentKey.LORE, playerStack) ? 1 - lorePenalty : 1.0;

        double multiplier = Math.max(damageMultiplier * repairMultiplier * nameMultiplier * loreMultiplier, minValueFraction);
        long payout = Math.round(listing.sellPrice() * multiplier);
        return Math.max(payout, 1);
    }

    /**
     * Returns {@code 1.0} if the item isn't damageable, has no max damage, or hasn't been damaged. Otherwise:
     * <pre>{@code
     * remainingDurabilityFraction = (maxDamage - damage) / maxDamage
     * damageMultiplier = minValueFraction
     *     + (1 - firstUsePenalty - minValueFraction) * remainingDurabilityFraction^damageCurveExponent
     * }</pre>
     */
    private double damageMultiplier(IItemStack listed, IItemStack actual) {
        if (!actual.isDamageable()) return 1.0;

        int maxDamage = actual.maxDamage();
        if (maxDamage == 0) return 1.0;

        int listedDamage = listed.damage();
        int damage = actual.damage();
        if (damage <= listedDamage) return 1.0;

        double remainingDurabilityFraction = (double) (maxDamage - damage) / maxDamage;
        double floor = minValueFraction;
        return floor + (1 - firstUsePenalty - floor) * Math.pow(remainingDurabilityFraction, damageCurveExponent);
    }

    /**
     * <pre>{@code
     * delta = max(0, repairCost - listedRepairCost)
     * repairMultiplier = max(minValueFraction, 1 - delta * repairCostPenaltyPerPoint)
     * }</pre>
     */
    private double repairMultiplier(IItemStack listed, IItemStack actual) {
        int delta = Math.max(0, actual.repairCost() - listed.repairCost());
        return Math.max(minValueFraction, 1 - delta * repairCostPenaltyPerPoint);
    }
}
