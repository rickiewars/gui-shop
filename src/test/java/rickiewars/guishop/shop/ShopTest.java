package rickiewars.guishop.shop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopTest extends MinecraftTest {

    ShopItem stoneItem;
    ShopItem dirtItem;
    ResourceId defaultCurrency;

    Shop shop;

    private CapturingAppender logAppender;
    private Logger guiShopLogger;

    @AfterEach
    void resetGlobalState() {
        GUIShop.shops = new java.util.LinkedList<>();
    }

    @BeforeEach
    void attachLogCapture() {
        guiShopLogger = (Logger) LogManager.getLogger("gui-shop");
        logAppender = new CapturingAppender();
        logAppender.start();
        guiShopLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogCapture() {
        guiShopLogger.removeAppender(logAppender);
        logAppender.stop();
    }

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
            ResourceId.parse("test:currency"),
            List.of("Just some dirt")
        );

        defaultCurrency = ResourceId.parse("test:default");

        shop = new Shop(
            "test_shop",
            "TestShop",
            List.of(stoneItem, dirtItem),
            defaultCurrency,
            ResourceId.ofVanilla("ender_chest"),
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
        wornListing.setDamageValue(200);
        ShopItem expensiveWorn = new ShopItem("Expensive worn", new MinecraftItemStack(wornListing), 100, 1000, null, List.of());

        Shop s = new Shop("s", "S", List.of(cheapPristine, expensiveWorn), null, null, SellPricing.DEFAULT);

        ItemStack playerStack = new ItemStack(Items.IRON_PICKAXE);
        playerStack.setDamageValue(200); // matches expensiveWorn's baseline exactly -> multiplier 1.0

        ShopItem found = s.findHighestPayingItem(new MinecraftItemStack(playerStack));
        assertEquals(expensiveWorn, found);
    }

    @Test
    void getCurrencyIdReturnsItemCurrencyIfPresent() {
        ResourceId currency = shop.getCurrencyId(dirtItem);

        assertEquals(dirtItem.resolvedCurrencyId(), currency);
    }

    @Test
    void getCurrencyIdFallsBackToDefaultCurrency() {
        ResourceId currency = shop.getCurrencyId(stoneItem);

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

        ResourceId currency = noDefaultCurrencyShop.getDefaultCurrencyId();

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

    @Test
    void findByNameReturnsMatchingShopByDisplayName() {
        GUIShop.shops = List.of(shop);
        assertSame(shop, Shop.findByName("TestShop"));
    }

    @Test
    void findByNameReturnsNullWhenNoMatch() {
        GUIShop.shops = List.of(shop);
        assertNull(Shop.findByName("Other Shop"));
    }

    @Test
    void findByNameIsCaseSensitive() {
        GUIShop.shops = List.of(shop);
        assertNull(Shop.findByName("testshop"));
    }

    @Test
    void validateLogsNothingForValidItems() {
        shop.validate();

        assertTrue(logAppender.messages.isEmpty());
    }

    @Test
    void validateWarnsOnInvalidBuyPrice() {
        ShopItem invalidBuyPrice = new ShopItem("Broken", new MinecraftItemStack(new ItemStack(Items.STONE)), -2, 5, null, List.of());
        Shop s = new Shop("s", "S", List.of(invalidBuyPrice), null, null, SellPricing.DEFAULT);

        s.validate();

        assertEquals(1, logAppender.messages.size());
        assertTrue(logAppender.messages.get(0).contains("invalid buyPrice"));
    }

    @Test
    void validateWarnsOnInvalidSellPrice() {
        ShopItem invalidSellPrice = new ShopItem("Broken", new MinecraftItemStack(new ItemStack(Items.STONE)), 5, -2, null, List.of());
        Shop s = new Shop("s", "S", List.of(invalidSellPrice), null, null, SellPricing.DEFAULT);

        s.validate();

        assertEquals(1, logAppender.messages.size());
        assertTrue(logAppender.messages.get(0).contains("invalid sellPrice"));
    }

    @Test
    void validateWarnsWhenSellPriceExceedsBuyPrice() {
        ShopItem moneyLoop = new ShopItem("Loop", new MinecraftItemStack(new ItemStack(Items.STONE)), 10, 20, null, List.of());
        Shop s = new Shop("s", "S", List.of(moneyLoop), null, null, SellPricing.DEFAULT);

        s.validate();

        assertEquals(1, logAppender.messages.size());
        assertTrue(logAppender.messages.get(0).contains("money loop"));
    }

    @Test
    void validateDoesNotWarnWhenBuyOrSellIsDisabled() {
        ShopItem sellOnly = new ShopItem("SellOnly", new MinecraftItemStack(new ItemStack(Items.STONE)), -1, 5, null, List.of());
        ShopItem buyOnly = new ShopItem("BuyOnly", new MinecraftItemStack(new ItemStack(Items.STONE)), 5, -1, null, List.of());
        Shop s = new Shop("s", "S", List.of(sellOnly, buyOnly), null, null, SellPricing.DEFAULT);

        s.validate();

        assertTrue(logAppender.messages.isEmpty());
    }

    @Test
    void validateWarnsSeparatelyForEachInvalidItem() {
        ShopItem invalidBuyPrice = new ShopItem("Broken1", new MinecraftItemStack(new ItemStack(Items.STONE)), -2, 5, null, List.of());
        ShopItem moneyLoop = new ShopItem("Broken2", new MinecraftItemStack(new ItemStack(Items.DIRT)), 10, 20, null, List.of());
        Shop s = new Shop("s", "S", List.of(invalidBuyPrice, moneyLoop), null, null, SellPricing.DEFAULT);

        s.validate();

        assertEquals(2, logAppender.messages.size());
    }

    private static class CapturingAppender extends AbstractAppender {
        final List<String> messages = new ArrayList<>();

        CapturingAppender() {
            super("capturing-test-appender", null, null, false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }
    }
}
