package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.economy.EconomyUtils;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class GUIShopAddItemCommandTest extends CommandTestBase {

    @GameTest
    public void addItemAddsItemWithAllArgumentsIncludingMultilineDescription(GameTestHelper context) {
        Shop shop = new Shop("multi_shop", "Multi Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Multi Shop\" \"Diamond\" minecraft:diamond 10 5 test:coins \"Line one\\\\Line two\"");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));

        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.buyPrice(), 10L, Component.literal("buy price"));
        context.assertValueEqual(item.sellPrice(), 5L, Component.literal("sell price"));
        context.assertValueEqual(item.explicitCurrencyId(), ResourceId.of("test", "coins"), Component.literal("currency"));
        context.assertValueEqual(item.description(), List.of("Line one", "Line two"), Component.literal("description split on backslash"));
        context.succeed();
    }

    @GameTest
    public void addItemDescriptionDefaultsToEmptyWhenOmitted(GameTestHelper context) {
        Shop shop = new Shop("defaults_shop", "Defaults Shop");
        GUIShop.shops.add(shop);

        dispatch(context,
            "guishop additem \"Defaults Shop\" \"Diamond\" minecraft:diamond 10 5 test:coins");

        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));
        ShopItem item = shop.getItems().get(0);
        context.assertTrue(item.description().isEmpty(), Component.literal("expected an empty description but got " + item.description().size() + " line(s)"));
        context.succeed();
    }

    @GameTest
    public void addItemCurrencyDefaultsToShopDefaultCurrencyWhenOmitted(GameTestHelper context) {
        ResourceId shopDefaultCurrency = ResourceId.of("test", "shop_coins");
        Shop shop = new Shop("currency_defaults_shop", "Currency Defaults Shop", new java.util.LinkedList<>(), shopDefaultCurrency);
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Currency Defaults Shop\" \"Diamond\" minecraft:diamond 10 5");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));

        ShopItem item = shop.getItems().get(0);
        context.assertTrue(!item.hasCurrency(), Component.literal("no explicit currency should be recorded on the item"));
        context.assertValueEqual(shop.getCurrencyId(item), shopDefaultCurrency, Component.literal("item's resolved currency"));
        context.succeed();
    }

    @GameTest
    public void addItemCurrencyFallsBackToGlobalDefaultWhenShopHasNoDefaultCurrency(GameTestHelper context) {
        Shop shop = new Shop("no_default_currency_shop", "No Default Currency Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"No Default Currency Shop\" \"Diamond\" minecraft:diamond 10 5");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));

        ShopItem item = shop.getItems().get(0);
        context.assertTrue(!item.hasCurrency(), Component.literal("no explicit currency should be recorded on the item"));
        context.assertValueEqual(shop.getCurrencyId(item), EconomyUtils.getFirstCurrencyId(), Component.literal("item's resolved currency"));
        context.succeed();
    }

    @GameTest
    public void addItemReportsErrorWhenShopNotFound(GameTestHelper context) {
        var capture = dispatch(context,
            "guishop additem \"Ghost Shop\" \"Diamond\" minecraft:diamond 10 5 test:coins");

        context.assertTrue(capture.anyMessageContains("does not exist"), Component.literal("expected a not-found message"));
        context.succeed();
    }

    @GameTest
    public void addItemNegativeBuyPriceDisablesBuying(GameTestHelper context) {
        Shop shop = new Shop("price_shop", "Price Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Price Shop\" \"Diamond\" minecraft:diamond -1 5 test:coins");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.buyPrice(), -1L, Component.literal("buy price"));
        context.assertValueEqual(item.sellPrice(), 5L, Component.literal("sell price"));
        context.succeed();
    }

    @GameTest
    public void addItemNegativeSellPriceDisablesSelling(GameTestHelper context) {
        Shop shop = new Shop("price_shop", "Price Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Price Shop\" \"Diamond\" minecraft:diamond 5 -1 test:coins");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.buyPrice(), 5L, Component.literal("buy price"));
        context.assertValueEqual(item.sellPrice(), -1L, Component.literal("sell price"));
        context.succeed();
    }

    @GameTest
    public void addItemAnyNegativeSellPriceNormalizesToDisabled(GameTestHelper context) {
        Shop shop = new Shop("price_shop", "Price Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Price Shop\" \"Diamond\" minecraft:diamond 5 -5 test:coins");

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after additem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.sellPrice(), -1L, Component.literal("sell price"));
        context.succeed();
    }

    @GameTest
    public void addItemRejectsWhenBothPricesAreDisabled(GameTestHelper context) {
        Shop shop = new Shop("price_shop", "Price Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop additem \"Price Shop\" \"Diamond\" minecraft:diamond -1 -1 test:coins");

        context.assertTrue(capture.anyMessageContains("cannot have both"), Component.literal("expected a both-disabled message"));
        context.assertValueEqual(shop.getItems().size(), 0, Component.literal("item count after additem with both prices disabled"));
        context.succeed();
    }

    @GameTest
    public void addItemRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.ADD_ITEM.defaultLevel(),
            source -> dispatch(context,
                "guishop additem \"Sweep Shop\" \"Diamond\" minecraft:diamond 10 5 test:coins", source),
            result -> result.anyMessageContains("successfully added"));
        context.succeed();
    }

    @GameTest
    public void addItemRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop additem \"Sweep Shop\" \"Diamond\" minecraft:diamond 10 5 test:coins");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.ADD_ITEM);
        context.succeed();
    }
}
