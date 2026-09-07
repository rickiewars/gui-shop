package rickiewars.guishop.command.economy.subcommands;

import eu.pb4.common.economy.api.EconomyAccount;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.economy.EconomyCommandTest;

public class GUIShopBalanceAddCommandTest extends EconomyCommandTest {

    @GameTest
    public void addIncreasesTargetPlayerBalance(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);

        var capture = dispatch(context,
            "guishop balance guishop:credit add @s 50", playerSource(target));

        context.assertTrue(capture.anyMessageContains("Successfully added"), "expected a success message");
        context.assertValueEqual(account.balance(), 50L, "balance after add");
        context.succeed();
    }

    @GameTest
    public void addRejectsNonPositiveAmount(GameTestHelper context) {
        ServerPlayer target = mockPlayer(context);
        EconomyAccount account = creditAccount(context, target);

        var capture = dispatch(context,
            "guishop balance guishop:credit add @s 0", playerSource(target));

        context.assertTrue(capture.anyMessageContains("must be greater than 0"), "expected a validation message");
        context.assertValueEqual(account.balance(), 0L, "balance after a rejected add");
        context.succeed();
    }

    @GameTest
    public void addRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.BALANCE_ADD.defaultLevel(),
            source -> dispatch(context, "guishop balance guishop:credit add @s 50", source),
            result -> result.anyMessageContains("Successfully added"));
        context.succeed();
    }

    @GameTest
    public void addRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop balance guishop:credit add @s 50");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.BALANCE, GuiShopPermission.BALANCE_ADD);
        context.succeed();
    }
}
