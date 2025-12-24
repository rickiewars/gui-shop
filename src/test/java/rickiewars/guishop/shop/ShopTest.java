package rickiewars.guishop.shop;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopTest extends MinecraftTest {

    ShopItem stoneItem;
    ShopItem dirtItem;
    Identifier defaultCurrency;

    Shop shop;

    @BeforeEach
    void setup() {
        stoneItem = new ShopItem(
            "Fancy Stone",
            "minecraft:stone",
            10,
            5,
            null,
            new String[]{"A fancy stone block"},
            null
        );

        dirtItem = new ShopItem(
            "Cheap Dirt",
            "minecraft:dirt",
            20,
            10,
            Identifier.of("test:currency"),
            new String[]{"Just some dirt"},
            null
        );

        defaultCurrency = Identifier.of("test:default");

        shop = new Shop(
            "TestShop",
            List.of(stoneItem, dirtItem),
            defaultCurrency
        );
    }

    @Test
    void findItemReturnsMatchingShopItem() {
        ItemStack stack = new ItemStack(Items.STONE, 1);

        ShopItem found = shop.findItem(stack);

        assertNotNull(found);
        assertEquals(stoneItem, found);
    }

    @Test
    void findItemReturnsNullIfItemNotInShop() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 1);

        ShopItem found = shop.findItem(stack);

        assertNull(found);
    }

    @Test
    void findItemCanFindEnchantedItems() {
        ItemStack stack = new ItemStack(Items.STONE, 1);
        stack.set(DataComponentTypes.RARITY, Rarity.EPIC);

        ShopItem found = shop.findItem(stack);

        assertNull(found);
    }

    @Test
    void getCurrencyIdReturnsItemCurrencyIfPresent() {
        Identifier currency = shop.getCurrencyId(dirtItem);

        assertEquals(dirtItem.currencyId(), currency);
    }

    @Test
    void getCurrencyIdFallsBackToDefaultCurrency() {
        Identifier currency = shop.getCurrencyId(stoneItem);

        assertEquals(defaultCurrency, currency);
    }

    @Test
    void getDefaultCurrencyIdFallsBackToEconomyUtilsIfNotConfigured() {
        Shop noDefaultCurrencyShop = new Shop(
            "NoDefault",
            List.of(stoneItem),
            null
        );

        Identifier currency = noDefaultCurrencyShop.getDefaultCurrencyId();

        assertEquals(EconomyUtils.getFirstCurrencyId(), currency);
    }

    @Test
    void hasDefaultCurrencyReflectsConfiguration() {
        assertTrue(shop.hasDefaultCurrency());

        Shop noDefaultCurrencyShop = new Shop(
            "NoDefault",
            List.of(stoneItem),
            null
        );

        assertFalse(noDefaultCurrencyShop.hasDefaultCurrency());
    }

    @Test
    void getNameReturnsConfiguredName() {
        assertEquals("TestShop", shop.getName());
    }

    @Test
    void getItemsReturnsConfiguredItems() {
        var items = shop.getItems();

        assertEquals(2, items.size());
        assertTrue(items.contains(stoneItem));
        assertTrue(items.contains(dirtItem));
    }
}
