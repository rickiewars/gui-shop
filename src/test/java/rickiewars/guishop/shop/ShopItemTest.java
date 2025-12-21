package rickiewars.guishop.shop;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShopItemTest extends MinecraftTest {

    // ----------------------------------------------------------
    // Basic construction tests
    // ----------------------------------------------------------

    @Test
    void shopItemStoresBasicFields() {
        ShopItem item = new ShopItem(
            "Test Sword",
            "minecraft:diamond_sword",
            100,
            50,
            null,
            new String[]{"A blade"},
            null
        );

        assertEquals("Test Sword", item.itemName());
        assertEquals("minecraft:diamond_sword", item.itemId());
        assertEquals(100, item.buyItemPrice());
        assertEquals(50, item.sellItemPrice());
        assertArrayEquals(new String[]{"A blade"}, item.description());
        assertFalse(item.hasCurrency());
        assertFalse(item.hasComponentChanges());
        assertEquals(GuiShopEconomyCurrency.DEFAULT_ID, item.currencyId());
    }

    @Test
    void shopItemStoresCurrency() {
        Identifier currency = Identifier.of("guishop:credit");

        ShopItem item = new ShopItem(
            "Credit Item",
            "minecraft:stone",
            10,
            5,
            currency,
            new String[]{"Simple item"},
            null
        );

        assertTrue(item.hasCurrency());
        assertEquals(currency, item.currencyId());
    }

    @Test
    void shopItemStoresComponentChanges() {
        ItemStack stack = new ItemStack(Items.APPLE);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Golden Apple"));
        ComponentChanges changes = stack.getComponentChanges();

        ShopItem item = new ShopItem(
            "Special Apple",
            "minecraft:apple",
            10,
            3,
            null,
            new String[]{"Magical"},
            changes
        );

        assertTrue(item.hasComponentChanges());
        assertEquals(
            changes.get(DataComponentTypes.CUSTOM_NAME),
            item.componentChanges().get(DataComponentTypes.CUSTOM_NAME)
        );
    }

    // ----------------------------------------------------------
    // matches(ItemStack)
    // ----------------------------------------------------------

    @Test
    void matchesFailsWhenItemIdDiffers() {
        ShopItem shopItem = new ShopItem(
            "Stone",
            "minecraft:stone",
            1, 0,
            null,
            new String[]{},
            null
        );

        ItemStack matching = new ItemStack(Items.STONE);
        ItemStack nonMatching = new ItemStack(Items.DIRT);

        assertTrue(shopItem.matches(matching));
        assertFalse(shopItem.matches(nonMatching));
    }

    @Test
    void matchesIgnoresNameChanges() {
        ItemStack changed = new ItemStack(Items.IRON_SWORD);
        changed.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Knight Blade"));

        ComponentChanges required = changed.getComponentChanges();

        ShopItem item = new ShopItem(
            "Knight Sword",
            "minecraft:iron_sword",
            50, 25,
            null,
            new String[]{"Strong"},
            required
        );

        ItemStack itemStack = new ItemStack(Items.IRON_SWORD);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("My custom Knight Blade"));

        assertTrue(item.matches(itemStack));
    }

    @Test
    void matchesFailsWhenRarityDiffers() {
        // Create required componentChanges: RARITY is one of the relevant types
        ItemStack rareSword = new ItemStack(Items.IRON_SWORD);
        rareSword.set(DataComponentTypes.RARITY, Rarity.EPIC);

        ComponentChanges required = rareSword.getComponentChanges();

        ShopItem item = new ShopItem(
            "Rare Sword",
            "minecraft:iron_sword",
            100, 50,
            null,
            new String[]{"Valuable"},
            required
        );

        // Matching sword: same rarity
        ItemStack matching = new ItemStack(Items.IRON_SWORD);
        matching.set(DataComponentTypes.RARITY, Rarity.EPIC);

        // Non-matching: different rarity
        ItemStack nonMatching = new ItemStack(Items.IRON_SWORD);
        nonMatching.set(DataComponentTypes.RARITY, Rarity.COMMON);

        assertTrue(item.matches(matching),
            "Sword with same rarity should match");

        assertFalse(item.matches(nonMatching),
            "Sword with different rarity should NOT match based on relevant component type");
    }

    @Test
    void matchesFailsWhenCustomModelDataDiffers() {
        ItemStack template = new ItemStack(Items.DIAMOND_SWORD);
        template.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);

        ComponentChanges required = template.getComponentChanges();

        ShopItem item = new ShopItem(
            "Model Sword",
            "minecraft:diamond_sword",
            100, 50,
            null,
            new String[]{"Unique look"},
            required
        );

        ItemStack matching = new ItemStack(Items.DIAMOND_SWORD);
        matching.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);

        ItemStack nonMatching = new ItemStack(Items.DIAMOND_SWORD);
        nonMatching.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(1.2f), List.of(), List.of(), List.of()));

        assertTrue(item.matches(matching),
            "Extra irrelevant components must NOT break matching");

        assertFalse(item.matches(nonMatching),
            "Different relevant component type (custom model data) must break matching");
    }
}
