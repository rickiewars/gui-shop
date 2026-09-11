package rickiewars.guishop.command.player;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class GUIShopListCommandTest extends CommandTestBase {

    @GameTest
    public void listAllShopsShowsNamesAndMarksEmptyShopsOutOfStock(GameTestHelper context) {
        Shop stocked = new Shop("stocked_shop", "Stocked Shop");
        stocked.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        Shop empty = new Shop("empty_shop", "Empty Shop");
        GUIShop.shops.add(stocked);
        GUIShop.shops.add(empty);

        var result = dispatch(context, "guishop list");

        context.assertTrue(result.anyMessageContains("Stocked Shop"), Component.literal("expected the stocked shop to be listed"));
        context.assertTrue(result.anyMessageContains("Empty Shop (Out of stock)"), Component.literal("expected the empty shop to be marked out of stock"));
        context.assertTrue(!result.anyMessageContains("Stocked Shop (Out of stock)"), Component.literal("the stocked shop must not be marked out of stock"));
        context.succeed();
    }

    @GameTest
    public void listAllShopsReportsErrorWhenNoneAvailable(GameTestHelper context) {
        var result = dispatch(context, "guishop list");

        context.assertTrue(result.anyMessageContains("no shops available"), Component.literal("expected a no-shops-available message"));
        context.succeed();
    }

    @GameTest
    public void listSpecificShopShowsItsItems(GameTestHelper context) {
        Shop shop = new Shop("stocked_shop", "Stocked Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        var result = dispatch(context, "guishop list \"Stocked Shop\"");

        context.assertTrue(result.anyMessageContains("Item name: Sword"), Component.literal("expected the item name to be listed"));
        context.succeed();
    }

    @GameTest
    public void listSpecificShopReportsErrorWhenNotFound(GameTestHelper context) {
        var result = dispatch(context, "guishop list \"Ghost Shop\"");

        context.assertTrue(result.anyMessageContains("does not exist"), Component.literal("expected a not-found message"));
        context.succeed();
    }

    @GameTest
    public void listAllShopsRequiresVanillaPermission(GameTestHelper context) {
        GUIShop.shops.add(new Shop("sweep_shop", "Sweep Shop"));

        assertVanillaLevel(context, GuiShopPermission.LIST.defaultLevel(),
            source -> dispatch(context, "guishop list", source),
            result -> result.anyMessageContains("Sweep Shop"));
        context.succeed();
    }

    @GameTest
    public void listSpecificShopRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.LIST_ITEMS.defaultLevel(),
            source -> dispatch(context, "guishop list \"Sweep Shop\"", source),
            result -> result.anyMessageContains("Item name: Sword"));
        context.succeed();
    }

    @GameTest
    public void listAllShopsRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop list");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.LIST);
        context.succeed();
    }

    @GameTest
    public void listSpecificShopRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop list \"Sweep Shop\"");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.LIST, GuiShopPermission.LIST_ITEMS);
        context.succeed();
    }
}
