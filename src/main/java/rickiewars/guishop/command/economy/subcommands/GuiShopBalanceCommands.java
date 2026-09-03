package rickiewars.guishop.command.economy.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.errors.CommandErrors;

import java.util.Optional;

public class GuiShopBalanceCommands {

    protected static EconomyCurrency getCurrency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier currencyId = getCurrencyId(context).orElse(GuiShopEconomyCurrency.DEFAULT_ID);
        var result = CommonEconomy.getCurrency(context.getSource().getServer(), currencyId);
        if (result == null) throw CommandErrors.CURRENCY_NOT_FOUND.create(currencyId);
        return result;
    }

    protected static Optional<Identifier> getCurrencyId(CommandContext<CommandSourceStack> context) {
        try {
            return Optional.ofNullable(IdentifierArgument.getId(context, "currency"));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

}
