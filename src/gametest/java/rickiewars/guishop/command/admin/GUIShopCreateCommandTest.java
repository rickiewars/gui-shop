package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;

public class GUIShopCreateCommandTest extends CommandTestBase {

    @GameTest
    public void createShopAddsShopWithSlugifiedId(GameTestHelper context) {
        var capture = dispatch(context, "guishop create \"Cool Shop!\"");

        context.assertTrue(capture.anyMessageContains("successfully created"), Component.literal("expected a success message"));
        context.assertValueEqual(GUIShop.shops.size(), 1, Component.literal("shop count after create"));
        context.assertValueEqual(GUIShop.shops.get(0).getId(), "cool_shop", Component.literal("slugified shop id"));
        context.assertValueEqual(GUIShop.shops.get(0).getDisplayName(), "Cool Shop!", Component.literal("shop display name"));
        context.succeed();
    }

    @GameTest
    public void createShopWithCollidingSlugGetsUniqueSuffix(GameTestHelper context) {
        dispatch(context, "guishop create \"Cool Shop\"");
        dispatch(context, "guishop create \"COOL SHOP!!\"");

        context.assertValueEqual(GUIShop.shops.size(), 2, Component.literal("shop count after two colliding creates"));
        context.assertValueEqual(GUIShop.shops.get(0).getId(), "cool_shop", Component.literal("first shop id"));
        context.assertValueEqual(GUIShop.shops.get(1).getId(), "cool_shop_2", Component.literal("second shop id"));
        context.succeed();
    }

    @GameTest
    public void createShopRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.CREATE_SHOP.defaultLevel(),
            source -> dispatch(context, "guishop create \"Sweep Shop\"", source),
            result -> result.anyMessageContains("successfully created"));
        context.succeed();
    }

    @GameTest
    public void createShopRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop create \"Sweep Shop\"");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.CREATE_SHOP);
        context.succeed();
    }
}
