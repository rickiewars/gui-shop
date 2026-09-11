package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        context.assertValueEqual(shop.getItems().getFirst().itemId(), ResourceId.ofVanilla("diamond_sword"), Component.literal("held item id"));
        context.assertValueEqual(shop.getItems().getFirst().description(), List.of("Line one", "Line two"), Component.literal("description split on backslash"));
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        List<String> description = shop.getItems().getFirst().description();
        context.assertTrue(description.isEmpty(), Component.literal("expected an empty description but got " + description.size() + " line(s)"));
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

        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        ShopItem item = shop.getItems().get(0);
        context.assertTrue(!item.hasCurrency(), Component.literal("no explicit currency should be recorded on the item"));
        context.assertValueEqual(shop.getCurrencyId(item), shopDefaultCurrency, Component.literal("item's resolved currency"));
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        context.assertValueEqual(
            shop.getItems().get(0).explicitCurrencyId(),
            ResourceId.of("test", "coins"),
            Component.literal("explicit currency should be recorded on the item")
        );
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

        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        ShopItem item = shop.getItems().get(0);
        context.assertTrue(!item.hasCurrency(), Component.literal("no explicit currency should be recorded on the item"));
        context.assertValueEqual(shop.getCurrencyId(item), EconomyUtils.getFirstCurrencyId(), Component.literal("item's resolved currency"));
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.buyPrice(), -1L, Component.literal("buy price"));
        context.assertValueEqual(item.sellPrice(), 5L, Component.literal("sell price"));
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.buyPrice(), 5L, Component.literal("buy price"));
        context.assertValueEqual(item.sellPrice(), -1L, Component.literal("sell price"));
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

        context.assertTrue(capture.anyMessageContains("successfully added"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after addhelditem"));
        ShopItem item = shop.getItems().get(0);
        context.assertValueEqual(item.sellPrice(), -1L, Component.literal("sell price"));
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

        context.assertTrue(capture.anyMessageContains("cannot have both"), Component.literal("expected a both-disabled message"));
        context.assertValueEqual(shop.getItems().size(), 0, Component.literal("item count after addhelditem with both prices disabled"));
        context.succeed();
    }

    @GameTest
    public void addHeldItemRequiresAPlayerSource(GameTestHelper context) {
        Shop shop = new Shop("held_shop", "Held Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context,
            "guishop addhelditem \"Held Shop\" \"Sword\" 20 10");

        context.assertTrue(capture.anyMessageContains("must specify a player"), Component.literal("console source should be rejected"));
        context.assertValueEqual(shop.getItems().size(), 0, Component.literal("item count after a console dispatch"));
        context.succeed();
    }

    @GameTest
    public void addHeldItemFailsWithEmptyHand(GameTestHelper context) {
        Shop shop = new Shop("held_shop", "Held Shop");
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context,
            "guishop addhelditem \"Held Shop\" \"Sword\" 20 10", playerSource(player));

        context.assertTrue(capture.anyMessageContains("must be holding an item"), Component.literal("expected an empty-hand message"));
        context.assertValueEqual(shop.getItems().size(), 0, Component.literal("item count after an empty-hand dispatch"));
        context.succeed();
    }

    @GameTest
    public void addHeldItemFailsEarlyWhenShopNotFound(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context,
            "guishop addhelditem \"Ghost Shop\" \"Sword\" 20 10", playerSource(player));

        context.assertTrue(capture.anyMessageContains("does not exist"),
            Component.literal("the shop lookup runs before the hand-empty check, so a missing shop should win"));
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
