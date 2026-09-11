package rickiewars.guishop.command.player;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class GUIShopOpenCommandTest extends CommandTestBase {

    @GameTest
    public void openOpensTheShopMenuForTheInvokingPlayer(GameTestHelper context) {
        Shop shop = new Shop("stocked_shop", "Stocked Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop open \"Stocked Shop\"", playerSource(player));

        context.assertTrue(result.success, Component.literal("expected the command to report success"));
        context.assertValueEqual(result.result, 0, Component.literal("command result code"));
        context.succeed();
    }

    @GameTest
    public void openReportsErrorWhenShopNotFound(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop open \"Ghost Shop\"", playerSource(player));

        context.assertTrue(result.anyMessageContains("does not exist"), Component.literal("expected a not-found message"));
        context.succeed();
    }

    @GameTest
    public void openReportsErrorWhenShopHasNoItems(GameTestHelper context) {
        GUIShop.shops.add(new Shop("empty_shop", "Empty Shop"));
        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop open \"Empty Shop\"", playerSource(player));

        context.assertTrue(result.anyMessageContains("is not available"), Component.literal("expected a shop-not-available message"));
        context.succeed();
    }

    @GameTest
    public void openRequiresAPlayerWhenNoTargetIsGiven(GameTestHelper context) {
        Shop shop = new Shop("stocked_shop", "Stocked Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        var result = dispatch(context, "guishop open \"Stocked Shop\"");

        context.assertTrue(result.anyMessageContains("must specify a player"), Component.literal("console source should be rejected"));
        context.succeed();
    }

    @GameTest
    public void openRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.OPEN.defaultLevel(),
            source -> dispatch(context, "guishop open \"Sweep Shop\"", source),
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void openForPlayerRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.OPEN_FOR_PLAYER.defaultLevel(),
            source -> dispatch(context, "guishop open \"Sweep Shop\" @s", source),
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void openRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop open \"Sweep Shop\"");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.OPEN);
        context.succeed();
    }

    @GameTest
    public void openForPlayerRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop open \"Sweep Shop\" @s");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.OPEN, GuiShopPermission.OPEN_FOR_PLAYER);
        context.succeed();
    }
}
