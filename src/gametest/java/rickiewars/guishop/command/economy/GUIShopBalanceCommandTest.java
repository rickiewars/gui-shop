package rickiewars.guishop.command.economy;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.GuiShopPermission;

public class GUIShopBalanceCommandTest extends EconomyCommandTest {

    @GameTest
    public void balanceShowsAllCurrencyBalances(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context, "guishop balance", playerSource(player));

        assertTrue(context, capture.anyMessageContains("Balance for Credits: $0.00"), "expected the credit balance to be listed");
        context.succeed();
    }

    @GameTest
    public void balanceRequiresAPlayerSource(GameTestHelper context) {
        var capture = dispatch(context, "guishop balance");

        assertTrue(context, capture.anyMessageContains("must specify a player"), "console source should be rejected");
        context.succeed();
    }

    @GameTest
    public void balanceForSpecificCurrencyShowsBalance(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context, "guishop balance guishop:credit", playerSource(player));

        assertTrue(context, capture.anyMessageContains("Balance for Credits: $0.00"), "expected the credit balance to be shown");
        context.succeed();
    }

    @GameTest
    public void balanceForUnknownCurrencyReportsError(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);

        var capture = dispatch(context, "guishop balance guishop:ghost_currency", playerSource(player));

        assertTrue(context, capture.anyMessageContains("does not exist"), "expected a currency-not-found message");
        context.succeed();
    }

    @GameTest
    public void balanceRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.BALANCE.defaultLevel(),
            source -> dispatch(context, "guishop balance", source),
            result -> result.anyMessageContains("Balance for"));
        context.succeed();
    }

    @GameTest
    public void balanceRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop balance");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.BALANCE);
        context.succeed();
    }
}
