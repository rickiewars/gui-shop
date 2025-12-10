package rickiewars.guishop.command.economy.subcommands;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;

public class GuiShopBalanceCommands {

    protected static EconomyCurrency getCurrency(CommandContext<ServerCommandSource> context) {
        Identifier currency = GuiShopEconomyCurrency.DEFAULT_ID;
        try {
            currency = IdentifierArgumentType.getIdentifier(context, "currency");
        } catch (IllegalArgumentException ignored) {
            GUIShop.LOGGER.error("Failed to get currency");
        }
        return CommonEconomy.getCurrency(context.getSource().getServer(), currency);
    }

}
