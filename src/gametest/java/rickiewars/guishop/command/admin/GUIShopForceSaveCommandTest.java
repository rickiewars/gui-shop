package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;

public class GUIShopForceSaveCommandTest extends CommandTestBase {

    @GameTest
    public void forceSaveSucceedsForAdmin(GameTestHelper context) {
        var capture = dispatch(context, "guishop forcesave");

        context.assertTrue(capture.anyMessageContains("successfully saved"), Component.literal("expected a success message"));
        context.succeed();
    }

    @GameTest
    public void forceSaveRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.FORCE_SAVE.defaultLevel(),
            source -> dispatch(context, "guishop forcesave", source),
            result -> result.anyMessageContains("successfully saved"));
        context.succeed();
    }

    @GameTest
    public void forceSaveRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop forcesave");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.FORCE_SAVE);
        context.succeed();
    }
}
