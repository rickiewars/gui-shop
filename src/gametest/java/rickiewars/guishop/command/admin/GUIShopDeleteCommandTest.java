package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;

public class GUIShopDeleteCommandTest extends CommandTestBase {

    @GameTest
    public void deleteShopRemovesExistingShop(GameTestHelper context) {
        GUIShop.shops.add(new Shop("to_delete", "To Delete"));

        var capture = dispatch(context, "guishop delete \"To Delete\"");

        context.assertTrue(capture.anyMessageContains("successfully removed"), "expected a success message");
        context.assertValueEqual(GUIShop.shops.size(), 0, "shop count after delete");
        context.succeed();
    }

    @GameTest
    public void deleteShopReportsErrorWhenNotFound(GameTestHelper context) {
        var capture = dispatch(context, "guishop delete \"Ghost Shop\"");

        context.assertTrue(capture.anyMessageContains("does not exist"), "expected a not-found message");
        context.assertValueEqual(GUIShop.shops.size(), 0, "shop count after deleting an unknown shop");
        context.succeed();
    }

    @GameTest
    public void deleteShopNameIsCaseSensitive(GameTestHelper context) {
        GUIShop.shops.add(new Shop("cool_shop", "Cool Shop"));

        var capture = dispatch(context, "guishop delete \"cool shop\"");

        context.assertTrue(capture.anyMessageContains("does not exist"), "display name match should be case-sensitive");
        context.assertValueEqual(GUIShop.shops.size(), 1, "shop count after a case-mismatched delete");
        context.succeed();
    }

    @GameTest
    public void deleteShopRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.DELETE_SHOP.defaultLevel(),
            source -> {
                GUIShop.shops.add(new Shop("sweep_shop", "Sweep Shop"));
                return dispatch(context, "guishop delete \"Sweep Shop\"", source);
            },
            result -> result.anyMessageContains("successfully removed"));
        context.succeed();
    }

    @GameTest
    public void deleteShopRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop delete \"Sweep Shop\"");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.DELETE_SHOP);
        context.succeed();
    }
}
