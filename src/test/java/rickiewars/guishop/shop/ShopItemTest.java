package rickiewars.guishop.shop;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShopItemTest extends MinecraftTest {

    private ShopItem item(ItemStack stack) {
        return new ShopItem("Test Item", new MinecraftItemStack(stack), 100, 50, null, List.of("A blade"));
    }

    private static IItemStack wrap(ItemStack stack) {
        return new MinecraftItemStack(stack);
    }

    // ----------------------------------------------------------
    // Basic construction / accessors
    // ----------------------------------------------------------

    @Test
    void shopItemStoresBasicFields() {
        ShopItem item = item(new ItemStack(Items.DIAMOND_SWORD));

        assertEquals("Test Item", item.displayName());
        assertEquals(Identifier.ofVanilla("diamond_sword"), item.itemId());
        assertEquals(100, item.buyPrice());
        assertEquals(50, item.sellPrice());
        assertEquals(List.of("A blade"), item.description());
        assertFalse(item.hasCurrency());
        assertFalse(item.hasComponentChanges());
        assertEquals(GuiShopEconomyCurrency.DEFAULT_ID, item.resolvedCurrencyId());
    }

    @Test
    void shopItemStoresCurrency() {
        Identifier currency = Identifier.of("guishop:credit");
        ShopItem item = new ShopItem("Credit Item", wrap(new ItemStack(Items.STONE)), 10, 5, currency, List.of());

        assertTrue(item.hasCurrency());
        assertEquals(currency, item.resolvedCurrencyId());
        assertEquals(currency, item.explicitCurrencyId());
    }

    @Test
    void isListableFalseOnlyWhenNeitherBuyableNorSellable() {
        assertTrue(new ShopItem("A", wrap(new ItemStack(Items.STONE)), 10, -1, null, List.of()).isListable());
        assertTrue(new ShopItem("A", wrap(new ItemStack(Items.STONE)), -1, 10, null, List.of()).isListable());
        assertFalse(new ShopItem("A", wrap(new ItemStack(Items.STONE)), -1, -1, null, List.of()).isListable());
    }

    // ----------------------------------------------------------
    // equals/hashCode -- ItemStack-safe, patch-shape-independent
    // ----------------------------------------------------------

    @Test
    void equalsUsesEffectiveComponentsNotPatchShape() {
        // Two stacks that end up with the same effective components via different patches
        // must be considered equal.
        ItemStack a = new ItemStack(Items.APPLE);
        a.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Shiny"));

        ItemStack b = new ItemStack(Items.APPLE);
        b.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Shiny"));

        ShopItem itemA = new ShopItem("Apple", wrap(a), 10, 5, null, List.of());
        ShopItem itemB = new ShopItem("Apple", wrap(b), 10, 5, null, List.of());

        assertEquals(itemA, itemB);
        assertEquals(itemA.hashCode(), itemB.hashCode());
    }

    @Test
    void equalsFalseWhenComponentsDiffer() {
        ItemStack a = new ItemStack(Items.APPLE);
        ItemStack b = new ItemStack(Items.APPLE);
        b.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Different"));

        ShopItem itemA = new ShopItem("Apple", wrap(a), 10, 5, null, List.of());
        ShopItem itemB = new ShopItem("Apple", wrap(b), 10, 5, null, List.of());

        assertNotEquals(itemA, itemB);
    }

    // ----------------------------------------------------------
    // resembles(ItemStack) -- deny-by-default (Strict unless Graded/Flat)
    // ----------------------------------------------------------

    @Test
    void resemblesFailsWhenItemDiffers() {
        ShopItem stone = item(new ItemStack(Items.STONE));
        assertTrue(stone.resembles(wrap(new ItemStack(Items.STONE))));
        assertFalse(stone.resembles(wrap(new ItemStack(Items.DIRT))));
    }

    @Test
    void resemblesIgnoresDamageAndRepairCostDifferences() {
        ItemStack listing = new ItemStack(Items.DIAMOND_SWORD);
        ShopItem shopItem = item(listing);

        ItemStack damaged = new ItemStack(Items.DIAMOND_SWORD);
        damaged.setDamage(500);
        damaged.set(DataComponentTypes.REPAIR_COST, 3);

        assertTrue(shopItem.resembles(wrap(damaged)), "Graded components must not block a sale");
    }

    @Test
    void resemblesIgnoresCustomNameAndLoreDifferences() {
        ShopItem shopItem = item(new ItemStack(Items.DIAMOND_SWORD));

        ItemStack renamed = new ItemStack(Items.DIAMOND_SWORD);
        renamed.set(DataComponentTypes.CUSTOM_NAME, Text.literal("My Sword"));

        assertTrue(shopItem.resembles(wrap(renamed)), "Flat-penalty components must not block a sale");
    }

    @Test
    void resemblesFailsOnAddedEnchantment() {
        ShopItem shopItem = item(new ItemStack(Items.DIAMOND_SWORD));

        ItemStack enchanted = new ItemStack(Items.DIAMOND_SWORD);
        enchanted.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        assertFalse(shopItem.resembles(wrap(enchanted)), "Strict components must block a sale on any difference");
    }

    @Test
    void resemblesFailsWhenCustomDataDiffers() {
        ShopItem shopItem = item(new ItemStack(Items.POTION));

        net.minecraft.nbt.NbtCompound customData = new net.minecraft.nbt.NbtCompound();
        customData.putString("marker", "x");
        ItemStack stamped = new ItemStack(Items.POTION);
        stamped.set(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(customData));

        assertFalse(shopItem.resembles(wrap(stamped)));
    }

    // ----------------------------------------------------------
    // matches(ItemStack) -- Strict AND Graded/Flat identical
    // ----------------------------------------------------------

    @Test
    void matchesRejectsDamagedItemThatResemblesAccepts() {
        ShopItem shopItem = item(new ItemStack(Items.DIAMOND_SWORD));

        ItemStack damaged = new ItemStack(Items.DIAMOND_SWORD);
        damaged.setDamage(10);

        assertTrue(shopItem.resembles(wrap(damaged)));
        assertFalse(shopItem.matches(wrap(damaged)));
    }

    @Test
    void matchesAcceptsPristineMatch() {
        ShopItem shopItem = item(new ItemStack(Items.DIAMOND_SWORD));
        assertTrue(shopItem.matches(wrap(new ItemStack(Items.DIAMOND_SWORD))));
    }
}
