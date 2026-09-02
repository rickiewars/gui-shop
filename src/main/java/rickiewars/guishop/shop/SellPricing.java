package rickiewars.guishop.shop;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

import java.util.Objects;

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

    public long adjustedPayout(ShopItem listing, ItemStack playerStack) {
        if (listing.sellPrice() == 0) return 0;

        double damageMultiplier = damageMultiplier(listing.stack(), playerStack);
        double repairMultiplier = repairMultiplier(listing.stack(), playerStack);
        double nameMultiplier = differs(listing.stack(), playerStack, DataComponentTypes.CUSTOM_NAME) ? 1 - customNamePenalty : 1.0;
        double loreMultiplier = differs(listing.stack(), playerStack, DataComponentTypes.LORE) ? 1 - lorePenalty : 1.0;

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
    private double damageMultiplier(ItemStack listed, ItemStack actual) {
        if (!actual.isDamageable()) return 1.0;

        int maxDamage = actual.getMaxDamage();
        if (maxDamage == 0) return 1.0;

        int listedDamage = listed.getDamage();
        int damage = actual.getDamage();
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
    private double repairMultiplier(ItemStack listed, ItemStack actual) {
        int listedRepairCost = listed.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
        int repairCost = actual.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
        int delta = Math.max(0, repairCost - listedRepairCost);
        return Math.max(minValueFraction, 1 - delta * repairCostPenaltyPerPoint);
    }

    private boolean differs(ItemStack listed, ItemStack actual, ComponentType<?> type) {
        return !Objects.equals(listed.get(type), actual.get(type));
    }
}
