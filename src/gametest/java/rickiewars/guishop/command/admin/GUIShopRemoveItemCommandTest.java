package rickiewars.guishop.command.admin;

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

public class GUIShopRemoveItemCommandTest extends CommandTestBase {

    @GameTest
    public void removeItemRemovesExistingItem(GameTestHelper context) {
        Shop shop = new Shop("remove_shop", "Remove Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        var capture = dispatch(context, "guishop removeitem \"Remove Shop\" \"Sword\"");

        context.assertTrue(capture.anyMessageContains("successfully removed"), Component.literal("expected a success message"));
        context.assertValueEqual(shop.getItems().size(), 0, Component.literal("item count after removeitem"));
        context.succeed();
    }

    @GameTest
    public void removeItemReportsErrorWhenShopNotFound(GameTestHelper context) {
        var capture = dispatch(context, "guishop removeitem \"Ghost Shop\" \"Sword\"");

        context.assertTrue(capture.anyMessageContains("does not exist"), Component.literal("expected a not-found message"));
        context.succeed();
    }

    @GameTest
    public void removeItemReportsErrorWhenItemNotFound(GameTestHelper context) {
        Shop shop = new Shop("remove_shop", "Remove Shop");
        GUIShop.shops.add(shop);

        var capture = dispatch(context, "guishop removeitem \"Remove Shop\" \"Sword\"");

        context.assertTrue(capture.anyMessageContains("was not found in this shop"), Component.literal("expected an item-not-found message"));
        context.succeed();
    }

    @GameTest
    public void removeItemNameIsCaseSensitive(GameTestHelper context) {
        Shop shop = new Shop("remove_shop", "Remove Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        var capture = dispatch(context, "guishop removeitem \"Remove Shop\" \"sword\"");

        context.assertTrue(capture.anyMessageContains("was not found in this shop"), Component.literal("item name match should be case-sensitive"));
        context.assertValueEqual(shop.getItems().size(), 1, Component.literal("item count after a case-mismatched removeitem"));
        context.succeed();
    }

    @GameTest
    public void removeItemRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.REMOVE_ITEM.defaultLevel(),
            source -> {
                shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
                return dispatch(context, "guishop removeitem \"Sweep Shop\" \"Sword\"", source);
            },
            result -> result.anyMessageContains("successfully removed"));
        context.succeed();
    }

    @GameTest
    public void removeItemRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop removeitem \"Sweep Shop\" \"Sword\"");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.REMOVE_ITEM);
        context.succeed();
    }
}
