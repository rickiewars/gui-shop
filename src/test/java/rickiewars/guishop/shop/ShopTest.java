package rickiewars.guishop.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
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
            new MinecraftItemStack(new ItemStack(Items.STONE)),
            10,
            5,
            null,
            List.of("A fancy stone block")
        );

        dirtItem = new ShopItem(
            "Cheap Dirt",
            new MinecraftItemStack(new ItemStack(Items.DIRT)),
            20,
            10,
            Identifier.of("test:currency"),
            List.of("Just some dirt")
        );

        defaultCurrency = Identifier.of("test:default");

        shop = new Shop(
            "test_shop",
            "TestShop",
            List.of(stoneItem, dirtItem),
            defaultCurrency,
            Identifier.ofVanilla("ender_chest"),
            SellPricing.DEFAULT
        );
    }

    @Test
    void findHighestPayingItemReturnsMatchingShopHighestPayingItem() {
        ItemStack stack = new ItemStack(Items.STONE, 1);

        ShopItem found = shop.findHighestPayingItem(new MinecraftItemStack(stack));

        assertNotNull(found);
        assertEquals(stoneItem, found);
    }

    @Test
    void findHighestPayingItemReturnsNullIfItemNotInShop() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 1);

        ShopItem found = shop.findHighestPayingItem(new MinecraftItemStack(stack));

        assertNull(found);
    }

    @Test
    void findHighestPayingItemExcludesNonSellableListing() {
        ShopItem notForSale = new ShopItem("Not sellable", new MinecraftItemStack(new ItemStack(Items.GOLD_INGOT)), 10, -1, null, List.of());
        Shop s = new Shop("s", "S", List.of(notForSale), null, null, SellPricing.DEFAULT);

        assertNull(s.findHighestPayingItem(new MinecraftItemStack(new ItemStack(Items.GOLD_INGOT))));
    }

    @Test
    void findHighestPayingItemRanksByAdjustedPayoutNotListedPrice() {
        // A pristine listing at a lower price should lose to a damaged listing whose adjusted
        // payout for THIS stack is higher.
        ShopItem cheapPristine = new ShopItem("Cheap", new MinecraftItemStack(new ItemStack(Items.IRON_PICKAXE)), 100, 100, null, List.of());

        ItemStack wornListing = new ItemStack(Items.IRON_PICKAXE);
        wornListing.setDamage(200);
        ShopItem expensiveWorn = new ShopItem("Expensive worn", new MinecraftItemStack(wornListing), 100, 1000, null, List.of());

        Shop s = new Shop("s", "S", List.of(cheapPristine, expensiveWorn), null, null, SellPricing.DEFAULT);

        ItemStack playerStack = new ItemStack(Items.IRON_PICKAXE);
        playerStack.setDamage(200); // matches expensiveWorn's baseline exactly -> multiplier 1.0

        ShopItem found = s.findHighestPayingItem(new MinecraftItemStack(playerStack));
        assertEquals(expensiveWorn, found);
    }

    @Test
    void getCurrencyIdReturnsItemCurrencyIfPresent() {
        Identifier currency = shop.getCurrencyId(dirtItem);

        assertEquals(dirtItem.resolvedCurrencyId(), currency);
    }

    @Test
    void getCurrencyIdFallsBackToDefaultCurrency() {
        Identifier currency = shop.getCurrencyId(stoneItem);

        assertEquals(defaultCurrency, currency);
    }

    @Test
    void getDefaultCurrencyIdFallsBackToEconomyUtilsIfNotConfigured() {
        Shop noDefaultCurrencyShop = new Shop(
            "no_default",
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
            "no_default",
            "NoDefault",
            List.of(stoneItem),
            null
        );

        assertFalse(noDefaultCurrencyShop.hasDefaultCurrency());
    }

    @Test
    void getDisplayNameReturnsConfiguredName() {
        assertEquals("TestShop", shop.getDisplayName());
    }

    @Test
    void equalsAndHashCodeAreKeyedOnId() {
        Shop sameIdDifferentName = new Shop("test_shop", "A totally different name");
        Shop differentId = new Shop("other_id", "TestShop");

        assertEquals(shop, sameIdDifferentName);
        assertEquals(shop.hashCode(), sameIdDifferentName.hashCode());
        assertNotEquals(shop, differentId);
    }

    @Test
    void getItemsReturnsConfiguredItems() {
        var items = shop.getItems();

        assertEquals(2, items.size());
        assertTrue(items.contains(stoneItem));
        assertTrue(items.contains(dirtItem));
    }

    @Test
    void iconResolvesToItemStack() {
        assertEquals(Items.ENDER_CHEST, shop.getIcon().getItem());

        Shop shopWithoutIcon = new Shop(
            "shop_without_icon",
            "ShopWithIcon",
            List.of(stoneItem),
            null
        );

        assertEquals(Items.CHEST, shopWithoutIcon.getIcon().getItem());
    }
}
