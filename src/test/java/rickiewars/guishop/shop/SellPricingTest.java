package rickiewars.guishop.shop;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.factories.SellPricingFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SellPricingTest extends MinecraftTest {

    private static final int TEST_MAX_DAMAGE = 100;

    private ShopItem listing(ItemStack stack, long sellPrice) {
        return new ShopItem("Listing", wrap(stack), 0, sellPrice, null, List.of());
    }

    private static IItemStack wrap(ItemStack stack) {
        return new MinecraftItemStack(stack);
    }

    /**
     * @param damage damage points out of {@link #TEST_MAX_DAMAGE} (100), so this doubles as a percentage
     */
    private ItemStack damagedItem(Item item, int damage) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.MAX_DAMAGE, TEST_MAX_DAMAGE);
        stack.setDamageValue(damage);
        return stack;
    }

    @Test
    void originalItemPaysFullPrice() {
        SellPricing pricing = new SellPricingFactory().build();
        ShopItem listing = listing(new ItemStack(Items.DIAMOND_SWORD), 1000);
        assertEquals(1000, pricing.adjustedPayout(listing, wrap(new ItemStack(Items.DIAMOND_SWORD))));
    }

    @Test
    void nonDamageableItemPaysFullPrice() {
        SellPricing pricing = new SellPricingFactory().build();
        ShopItem listing = listing(new ItemStack(Items.STONE), 100);
        assertEquals(100, pricing.adjustedPayout(listing, wrap(new ItemStack(Items.STONE))));
    }

    @Test
    void firstUsePenaltyAppliesImmediatelyOnSlightestDamage() {
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0.2)
            .withDamageCurveExponent(0)
            .build();
        ShopItem listing = listing(damagedItem(Items.IRON_PICKAXE, 0), 1000);
        ItemStack barelyDamaged = damagedItem(Items.IRON_PICKAXE, 1);

        assertEquals(800, pricing.adjustedPayout(listing, wrap(barelyDamaged)));
    }

    @Test
    void damagePenaltyScalesExponentially() {
        // half durability squared -> 25% of price
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0)
            .withDamageCurveExponent(2.0)
            .build();
        ShopItem listing = listing(damagedItem(Items.IRON_PICKAXE, 0), 1000);
        ItemStack halfDamaged = damagedItem(Items.IRON_PICKAXE, 50);

        assertEquals(250, pricing.adjustedPayout(listing, wrap(halfDamaged)));
    }

    @Test
    void damagePenaltyScalesLinearly() {
        // half durability linearly -> 50% of price
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0)
            .withDamageCurveExponent(1.0)
            .build();
        ShopItem listing = listing(damagedItem(Items.IRON_PICKAXE, 0), 1000);
        ItemStack halfDamaged = damagedItem(Items.IRON_PICKAXE, 50);

        assertEquals(500, pricing.adjustedPayout(listing, wrap(halfDamaged)));
    }

    @Test
    void damagePenaltyNeverDropsBelowMinValueFraction() {
        // fully damaged -> clamped to the 30% floor
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0)
            .withMinValueFraction(0.3)
            .withDamageCurveExponent(1.0)
            .build();
        ShopItem listing = listing(damagedItem(Items.IRON_PICKAXE, 0), 1000);
        ItemStack fullyDamaged = damagedItem(Items.IRON_PICKAXE, 100);

        assertEquals(300, pricing.adjustedPayout(listing, wrap(fullyDamaged)));
    }

    @Test
    void repairCostPenaltyScalesLinearlyWithRepairPoints() {
        // 1 repair points at 10% per point -> 90% of price
        SellPricing pricing = new SellPricingFactory()
            .withRepairCostPenaltyPerPoint(0.1)
            .build();
        ShopItem listing = listing(new ItemStack(Items.IRON_PICKAXE), 1000);
        ItemStack refurbishedItem = new ItemStack(Items.IRON_PICKAXE);
        refurbishedItem.set(DataComponents.REPAIR_COST, 1);

        assertEquals(900, pricing.adjustedPayout(listing, wrap(refurbishedItem)));
    }


    @Test
    void damageAndRepairPenaltiesStackByMultiplication() {
        // 3 items: damage-only, damage+repair, untouched
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0)
            .withMinValueFraction(0)
            .withDamageCurveExponent(1.0)
            .withRepairCostPenaltyPerPoint(0.1)
            .build();
        ShopItem listing = listing(damagedItem(Items.IRON_PICKAXE, 0), 1000);

        ItemStack broken = damagedItem(Items.IRON_PICKAXE, 75);

        ItemStack repaired = damagedItem(Items.IRON_PICKAXE, 25);
        repaired.set(DataComponents.REPAIR_COST, 2);

        ItemStack untouched = damagedItem(Items.IRON_PICKAXE, 0);

        assertEquals(250, pricing.adjustedPayout(listing, wrap(broken)));
        assertEquals(600, pricing.adjustedPayout(listing, wrap(repaired)));
        assertEquals(1000, pricing.adjustedPayout(listing, wrap(untouched)));
    }

    @Test
    void renamingPenalizedOnceAsFlatPenalty() {
        // 10% name penalty -> 90% of price
        SellPricing pricing = new SellPricingFactory().withCustomNamePenalty(0.1).build();
        ShopItem listing = listing(new ItemStack(Items.DIAMOND_SWORD), 1000);
        ItemStack renamed = new ItemStack(Items.DIAMOND_SWORD);
        renamed.set(DataComponents.CUSTOM_NAME, Component.literal("My Sword"));

        assertEquals(900, pricing.adjustedPayout(listing, wrap(renamed)));
    }

    @Test
    void lorePenaltyAppliesAlone() {
        // 10% lore penalty -> 90% of price
        SellPricing pricing = new SellPricingFactory().withLorePenalty(0.1).build();
        ShopItem listing = listing(new ItemStack(Items.DIAMOND_SWORD), 1000);
        ItemStack loredItem = new ItemStack(Items.DIAMOND_SWORD);
        loredItem.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Cool"))));

        assertEquals(900, pricing.adjustedPayout(listing, wrap(loredItem)));
    }

    @Test
    void nameAndLoreStackByMultiplication() {
        // 10% name penalty * 10% lore penalty -> 81% of price
        SellPricing pricing = new SellPricingFactory()
            .withCustomNamePenalty(0.1)
            .withLorePenalty(0.1)
            .build();
        ShopItem listing = listing(new ItemStack(Items.DIAMOND_SWORD), 1000);
        ItemStack renamedWithLore = new ItemStack(Items.DIAMOND_SWORD);
        renamedWithLore.set(DataComponents.CUSTOM_NAME, Component.literal("My Sword"));
        renamedWithLore.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Cool"))));

        assertEquals(810, pricing.adjustedPayout(listing, wrap(renamedWithLore)));
    }

    @Test
    void floorClampsExtremeStackedPenalties() {
        // fully damaged, repaired, renamed, and lored all at once -> clamped to the 10% floor
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(0.2)
            .withMinValueFraction(0.1)
            .withDamageCurveExponent(2.0)
            .withRepairCostPenaltyPerPoint(0.2)
            .withCustomNamePenalty(0.1)
            .withLorePenalty(0.1)
            .build();
        ShopItem listing = listing(damagedItem(Items.DIAMOND_SWORD, 0), 1000);

        ItemStack wrecked = damagedItem(Items.DIAMOND_SWORD, 100);
        wrecked.set(DataComponents.REPAIR_COST, 10);
        wrecked.set(DataComponents.CUSTOM_NAME, Component.literal("Wrecked"));
        wrecked.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Ruined"))));

        assertEquals(100, pricing.adjustedPayout(listing, wrap(wrecked)));
    }

    @Test
    void zeroSellPriceIgnoresPenalties() {
        SellPricing pricing = new SellPricingFactory().build();
        ShopItem listing = listing(damagedItem(Items.DIAMOND_SWORD, 0), 0);
        ItemStack damaged = damagedItem(Items.DIAMOND_SWORD, 100);

        assertEquals(0, pricing.adjustedPayout(listing, wrap(damaged)));
    }

    @Test
    void roundingNeverZerosOutAPositiveSellPrice() {
        SellPricing pricing = new SellPricingFactory()
            .withFirstUsePenalty(1.0)
            .withMinValueFraction(0)
            .build();
        ShopItem listing = listing(damagedItem(Items.DIAMOND_SWORD, 0), 1);
        ItemStack wrecked = damagedItem(Items.DIAMOND_SWORD, 99);

        assertEquals(1, pricing.adjustedPayout(listing, wrap(wrecked)));
    }
}
