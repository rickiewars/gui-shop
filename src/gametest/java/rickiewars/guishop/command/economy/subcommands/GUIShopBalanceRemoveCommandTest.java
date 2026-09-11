package rickiewars.guishop.command.economy.subcommands;

import eu.pb4.common.economy.api.EconomyAccount;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
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

        context.assertTrue(capture.anyMessageContains("Successfully removed"), Component.literal("expected a success message"));
        context.assertValueEqual(account.balance(), 60L, Component.literal("balance after remove"));
        context.succeed();
    }

    @GameTest
    public void removeFailsWithInsufficientFunds(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);

        var capture = dispatch(context,
            "guishop balance guishop:credit remove @s 50", playerSource(target));

        context.assertTrue(capture.anyMessageContains("Transaction failed"), Component.literal("expected a transaction-failed message"));
        context.assertValueEqual(account.balance(), 0L, Component.literal("balance after insufficient funds"));
        context.succeed();
    }

    @GameTest
    public void removeRejectsNonPositiveAmount(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);
        account.increaseBalance(100);

        var capture = dispatch(context,
            "guishop balance guishop:credit remove @s -1", playerSource(target));

        context.assertTrue(capture.anyMessageContains("must be greater than 0"), Component.literal("expected a validation message"));
        context.assertValueEqual(account.balance(), 100L, Component.literal("balance after a rejected remove"));
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
