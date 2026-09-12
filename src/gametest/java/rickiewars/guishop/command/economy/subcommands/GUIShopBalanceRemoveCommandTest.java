package rickiewars.guishop.command.economy.subcommands;

import eu.pb4.common.economy.api.EconomyAccount;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.economy.EconomyCommandTest;

public class GUIShopBalanceRemoveCommandTest extends EconomyCommandTest {

    @GameTest
    public void removeDecreasesTargetPlayerBalance(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);
        account.increaseBalance(100);

        var capture = dispatch(context,
            "guishop balance guishop:credit remove @s 40", playerSource(target));

        assertTrue(context, capture.anyMessageContains("Successfully removed"), "expected a success message");
        assertValueEqual(context, account.balance(), 60L, "balance after remove");
        context.succeed();
    }

    @GameTest
    public void removeFailsWithInsufficientFunds(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);

        var capture = dispatch(context,
            "guishop balance guishop:credit remove @s 50", playerSource(target));

        assertTrue(context, capture.anyMessageContains("Transaction failed"), "expected a transaction-failed message");
        assertValueEqual(context, account.balance(), 0L, "balance after insufficient funds");
        context.succeed();
    }

    @GameTest
    public void removeRejectsNonPositiveAmount(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);
        account.increaseBalance(100);

        var capture = dispatch(context,
            "guishop balance guishop:credit remove @s -1", playerSource(target));

        assertTrue(context, capture.anyMessageContains("must be greater than 0"), "expected a validation message");
        assertValueEqual(context, account.balance(), 100L, "balance after a rejected remove");
        context.succeed();
    }

    @GameTest
    public void removeRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.BALANCE_REMOVE.defaultLevel(),
            source -> {
                creditAccount(context, source.player()).increaseBalance(100);
                return dispatch(context, "guishop balance guishop:credit remove @s 40", source);
            },
            result -> result.anyMessageContains("Successfully removed"));
        context.succeed();
    }

    @GameTest
    public void removeRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop balance guishop:credit remove @s 40");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.BALANCE, GuiShopPermission.BALANCE_REMOVE);
        context.succeed();
    }
}
