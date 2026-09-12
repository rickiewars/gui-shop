package rickiewars.guishop.command.economy.subcommands;

import eu.pb4.common.economy.api.EconomyAccount;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.economy.EconomyCommandTest;

public class GUIShopBalanceSendCommandTest extends EconomyCommandTest {

    /**
     * Targets the other mock player with the nearest-player selector, filtered to a distance ring
     * that excludes the sender itself (distance 0) but includes a player placed a few blocks away
     * (see {@link EconomyCommandTest#placeAt}). Other tests' leftover mock players sit inside their
     * own, far-away test structures, so they never fall inside this ring.
     */
    private static final String OTHER_PLAYER_SELECTOR = "@p[distance=1..10]";

    @GameTest
    public void sendMovesBalanceBetweenPlayers(GameTestHelper context) {
        ServerPlayer sender = mockPlayer(context);
        ServerPlayer receiver = mockPlayer(context);
        placeAt(context, sender, new BlockPos(0, 0, 0));
        placeAt(context, receiver, new BlockPos(3, 0, 0));
        EconomyAccount senderAccount = creditAccount(context, sender);
        EconomyAccount receiverAccount = creditAccount(context, receiver);
        senderAccount.increaseBalance(100);

        var capture = dispatch(context,
            "guishop balance guishop:credit send " + OTHER_PLAYER_SELECTOR + " 30", playerSource(sender));

        assertTrue(context, capture.anyMessageContains("Successfully sent"), "expected a success message");
        assertValueEqual(context, senderAccount.balance(), 70L, "sender balance after send");
        assertValueEqual(context, receiverAccount.balance(), 30L, "receiver balance after send");
        context.succeed();
    }

    @GameTest
    public void sendRejectsTargetingSelf(GameTestHelper context) {
        ServerPlayer sender = mockPlayer(context);
        EconomyAccount senderAccount = creditAccount(context, sender);
        senderAccount.increaseBalance(100);

        var capture = dispatch(context,
            "guishop balance guishop:credit send @s 10", playerSource(sender));

        assertTrue(context, capture.anyMessageContains("cannot target yourself"), "expected a target-self message");
        assertValueEqual(context, senderAccount.balance(), 100L, "sender balance after a self-send attempt");
        context.succeed();
    }

    @GameTest
    public void sendFailsWithInsufficientFunds(GameTestHelper context) {
        ServerPlayer sender = mockPlayer(context);
        ServerPlayer receiver = mockPlayer(context);
        placeAt(context, sender, new BlockPos(0, 0, 0));
        placeAt(context, receiver, new BlockPos(3, 0, 0));
        EconomyAccount senderAccount = creditAccount(context, sender);
        EconomyAccount receiverAccount = creditAccount(context, receiver);

        var capture = dispatch(context,
            "guishop balance guishop:credit send " + OTHER_PLAYER_SELECTOR + " 10", playerSource(sender));

        assertTrue(context, capture.anyMessageContains("Transaction failed"), "expected a transaction-failed message");
        assertValueEqual(context, senderAccount.balance(), 0L, "sender balance after insufficient funds");
        assertValueEqual(context, receiverAccount.balance(), 0L, "receiver balance after insufficient funds");
        context.succeed();
    }

    @GameTest
    public void sendRequiresAPlayerSource(GameTestHelper context) {
        var capture = dispatch(context, "guishop balance guishop:credit send @s 10");

        assertTrue(context, capture.anyMessageContains("must specify a player"), "console source should be rejected");
        context.succeed();
    }

    @GameTest
    public void sendRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.BALANCE_SEND.defaultLevel(),
            source -> {
                creditAccount(context, source.player()).increaseBalance(100);
                return dispatch(context, "guishop balance guishop:credit send @s 10", source);
            },
            result -> result.anyMessageContains("cannot target yourself"));
        context.succeed();
    }

    @GameTest
    public void sendRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop balance guishop:credit send @s 10");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.BALANCE, GuiShopPermission.BALANCE_SEND);
        context.succeed();
    }
}
