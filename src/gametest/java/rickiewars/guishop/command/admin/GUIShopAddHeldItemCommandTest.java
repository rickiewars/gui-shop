package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.economy.EconomyUtils;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class GUIShopAddHeldItemCommandTest extends CommandTestBase {

    @GameTest
    public void addHeldItemAddsHeldItemStack(GameTestHelper context) {
        Shop shop = new Shop("held_shop", "Held Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Shop\" \"Sword\" 20 10 test:coins \"Line one\\\\Line two\"", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        assertValueEqual(context, shop.getItems().getFirst().itemId(), ResourceId.ofVanilla("diamond_sword"), "held item id");
        assertValueEqual(context, shop.getItems().getFirst().description(), List.of("Line one", "Line two"), "description split on backslash");
        context.succeed();
    }

    @GameTest
    public void addHeldItemDescriptionDefaultsToEmptyWhenOmitted(GameTestHelper context) {
        Shop shop = new Shop("held_defaults_shop", "Held Defaults Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Defaults Shop\" \"Sword\" 20 10", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        List<String> description = shop.getItems().getFirst().description();
        assertTrue(context, description.isEmpty(), "expected an empty description but got " + description.size() + " line(s)");
        context.succeed();
    }

    @GameTest
    public void addHeldItemCurrencyDefaultsToShopDefaultCurrencyWhenOmitted(GameTestHelper context) {
        ResourceId shopDefaultCurrency = ResourceId.of("test", "shop_coins");
        Shop shop = new Shop("held_currency_defaults_shop", "Held Currency Defaults Shop", new java.util.LinkedList<>(), shopDefaultCurrency);
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        dispatch(context,
            "guishop addhelditem \"Held Currency Defaults Shop\" \"Sword\" 20 10", playerSource(player));

        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        ShopItem item = shop.getItems().get(0);
        assertTrue(context, !item.hasCurrency(), "no explicit currency should be recorded on the item");
        assertValueEqual(context, shop.getCurrencyId(item), shopDefaultCurrency, "item's resolved currency");
        context.succeed();
    }

    @GameTest
    public void addHeldItemUsesExplicitCurrencyWhenProvided(GameTestHelper context) {
        Shop shop = new Shop("held_explicit_currency_shop", "Held Explicit Currency Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Explicit Currency Shop\" \"Sword\" 20 10 test:coins", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        assertValueEqual(context, shop.getItems().get(0).explicitCurrencyId(), ResourceId.of("test", "coins"), "explicit currency should be recorded on the item");
        context.succeed();
    }

    @GameTest
    public void addHeldItemCurrencyFallsBackToGlobalDefaultWhenShopHasNoDefaultCurrency(GameTestHelper context) {
        Shop shop = new Shop("held_no_default_currency_shop", "Held No Default Currency Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        dispatch(context,
            "guishop addhelditem \"Held No Default Currency Shop\" \"Sword\" 20 10", playerSource(player));

        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        ShopItem item = shop.getItems().get(0);
        assertTrue(context, !item.hasCurrency(), "no explicit currency should be recorded on the item");
        assertValueEqual(context, shop.getCurrencyId(item), EconomyUtils.getFirstCurrencyId(), "item's resolved currency");
        context.succeed();
    }

    @GameTest
    public void addHeldItemNegativeBuyPriceDisablesBuying(GameTestHelper context) {
        Shop shop = new Shop("held_price_shop", "Held Price Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Price Shop\" \"Sword\" -1 5 test:coins", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        ShopItem item = shop.getItems().get(0);
        assertValueEqual(context, item.buyPrice(), -1L, "buy price");
        assertValueEqual(context, item.sellPrice(), 5L, "sell price");
        context.succeed();
    }

    @GameTest
    public void addHeldItemNegativeSellPriceDisablesSelling(GameTestHelper context) {
        Shop shop = new Shop("held_price_shop", "Held Price Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Price Shop\" \"Sword\" 5 -1 test:coins", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        ShopItem item = shop.getItems().get(0);
        assertValueEqual(context, item.buyPrice(), 5L, "buy price");
        assertValueEqual(context, item.sellPrice(), -1L, "sell price");
        context.succeed();
    }

    @GameTest
    public void addHeldItemAnyNegativeSellPriceNormalizesToDisabled(GameTestHelper context) {
        Shop shop = new Shop("held_price_shop", "Held Price Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Price Shop\" \"Sword\" 5 -5 test:coins", playerSource(player));

        assertTrue(context, capture.anyMessageContains("successfully added"), "expected a success message");
        assertValueEqual(context, shop.getItems().size(), 1, "item count after addhelditem");
        ShopItem item = shop.getItems().get(0);
        assertValueEqual(context, item.sellPrice(), -1L, "sell price");
        context.succeed();
    }

    @GameTest
    public void addHeldItemRejectsWhenBothPricesAreDisabled(GameTestHelper context) {
        Shop shop = new Shop("held_price_shop", "Held Price Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

        var capture = dispatch(context,
            "guishop addhelditem \"Held Price Shop\" \"Sword\" -1 -1 test:coins", playerSource(player));

        assertTrue(context, capture.anyMessageContains("cannot have both"), "expected a both-disabled message");
        assertValueEqual(context, shop.getItems().size(), 0, "item count after addhelditem with both prices disabled");
        context.succeed();
    }

    @GameTest
    public void addHeldItemRequiresAPlayerSource(GameTestHelper context) {
        Shop shop = new Shop("held_shop", "Held Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop addhelditem \"Held Shop\" \"Sword\" 20 10");

        assertTrue(context, capture.anyMessageContains("must specify a player"), "console source should be rejected");
        assertValueEqual(context, shop.getItems().size(), 0, "item count after a console dispatch");
        context.succeed();
    }

    @GameTest
    public void addHeldItemFailsWithEmptyHand(GameTestHelper context) {
        Shop shop = new Shop("held_shop", "Held Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context,
            "guishop addhelditem \"Held Shop\" \"Sword\" 20 10", playerSource(player));

        assertTrue(context, capture.anyMessageContains("must be holding an item"), "expected an empty-hand message");
        assertValueEqual(context, shop.getItems().size(), 0, "item count after an empty-hand dispatch");
        context.succeed();
    }

    @GameTest
    public void addHeldItemFailsEarlyWhenShopNotFound(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context,
            "guishop addhelditem \"Ghost Shop\" \"Sword\" 20 10", playerSource(player));

        assertTrue(context, capture.anyMessageContains("does not exist"), "the shop lookup runs before the hand-empty check, so a missing shop should win");
        context.succeed();
    }

    @GameTest
    public void addHeldItemRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.ADD_ITEM.defaultLevel(),
            source -> {
                source.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
                return dispatch(context, "guishop addhelditem \"Sweep Shop\" \"Sword\" 20 10", source);
            },
            result -> result.anyMessageContains("successfully added"));
        context.succeed();
    }

    @GameTest
    public void addHeldItemRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop addhelditem \"Sweep Shop\" \"Sword\" 20 10");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.ADD_ITEM);
        context.succeed();
    }
}
