package rickiewars.guishop.command.player;

import eu.pb4.common.economy.api.EconomyAccount;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.economy.EconomyCommandTest;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class SellCommandTest extends CommandTestBase {

    @GameTest
    public void sellHandCreditsAccountAndClearsHeldItem(GameTestHelper context) {
        Shop shop = new Shop("sell_shop", "Sell Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), -1, 20, null, List.of()));
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        EconomyAccount account = EconomyCommandTest.creditAccount(context, player);

        dispatch(context, "sell hand", playerSource(player));

        context.assertTrue(account.balance() > 0, Component.literal("expected the account to be credited"));
        context.assertTrue(player.getMainHandItem().isEmpty(), Component.literal("expected the held item to be sold in full"));
        context.succeed();
    }

    @GameTest
    public void sellHandFailsWithEmptyHand(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        EconomyAccount account = EconomyCommandTest.creditAccount(context, player);

        var capture = dispatch(context, "sell hand", playerSource(player));

        context.assertTrue(capture.anyMessageContains("must be holding an item"), Component.literal("expected an empty-hand message"));
        context.assertValueEqual(account.balance(), 0L, Component.literal("balance after an empty-hand attempt"));
        context.succeed();
    }

    @GameTest
    public void sellHandFailsWhenHeldItemIsNotSellable(GameTestHelper context) {
        Shop shop = new Shop("sell_shop", "Sell Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), -1, -1, null, List.of()));
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        EconomyAccount account = EconomyCommandTest.creditAccount(context, player);

        var capture = dispatch(context, "sell hand", playerSource(player));

        context.assertTrue(capture.anyMessageContains("is not sellable in this shop"), Component.literal("expected a not-sellable message"));
        context.assertValueEqual(account.balance(), 0L, Component.literal("balance after a non-sellable item"));
        context.assertTrue(!player.getMainHandItem().isEmpty(), Component.literal("the held item should be untouched"));
        context.succeed();
    }

    @GameTest
    public void sellHandRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), -1, 20, null, List.of()));
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.SELL_HAND.defaultLevel(),
            source -> {
                source.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
                return dispatch(context, "sell hand", source);
            },
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void sellHandRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "sell hand");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.SELL, GuiShopPermission.SELL_HAND);
        context.succeed();
    }

}
