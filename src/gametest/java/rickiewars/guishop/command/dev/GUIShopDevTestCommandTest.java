package rickiewars.guishop.command.dev;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;

public class GUIShopDevTestCommandTest extends CommandTestBase {

    @GameTest
    public void devTestOpensTheMenuForAPermittedPlayer(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop devtest", playerSource(player));

        context.assertTrue(result.success, Component.literal("expected the command to report success"));
        context.assertValueEqual(result.result, 0, Component.literal("command result code"));
        context.succeed();
    }

    @GameTest
    public void devTestRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.TEST.defaultLevel(),
            source -> dispatch(context, "guishop devtest", source),
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void devTestRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop devtest");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.TEST);
        context.succeed();
    }
}
