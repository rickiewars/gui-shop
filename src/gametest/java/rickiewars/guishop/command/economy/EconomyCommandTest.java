package rickiewars.guishop.command.economy;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.command.CommandTestBase;

public abstract class EconomyCommandTest extends CommandTestBase {

    /**
     * Every mock player otherwise joins at the same position with the same name, so a
     * player-target argument can't pick one out by name or UUID. This repositions a mock player
     * relative to the test structure so tests needing a specific other player can target it with
     * a distance-filtered selector instead.
     */
    public static void placeAt(GameTestHelper context, ServerPlayer player, BlockPos relativePos) {
        BlockPos pos = context.absolutePos(relativePos);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    /** The built-in economy's default currency ("guishop:credit"), configured out of the box in config.json. */
    public static EconomyCurrency creditCurrency(GameTestHelper context) {
        MinecraftServer server = context.getLevel().getServer();
        EconomyCurrency currency = CommonEconomy.getCurrency(server, GuiShopEconomyCurrency.DEFAULT_ID.toIdentifier());
        if (currency == null) {
            throw new IllegalStateException(
                "guishop:credit currency is not configured, check config/gui-shop/config.json in the test run directory"
            );
        }
        return currency;
    }

    /** The player's account for the built-in credit currency; auto-created (via a mixin on PlayerList.placeNewPlayer) when the mock player joins. */
    public static EconomyAccount creditAccount(GameTestHelper context, ServerPlayer player) {
        EconomyCurrency currency = creditCurrency(context);
        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) {
            throw new IllegalStateException("no default credit account for " + player.getStringUUID());
        }
        return account;
    }
}
