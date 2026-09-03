package rickiewars.guishop.command.economy.subcommands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.errors.CommandErrors;

public class GUIShopBalanceAddCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add")
            .requires(GuiShopPermission.BALANCE_ADD.require())
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", LongArgumentType.longArg(-1))
                    .executes(GUIShopBalanceAddCommand::run)
                ));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        EconomyCurrency currency = GuiShopBalanceCommands.getCurrency(context);

        long amount = LongArgumentType.getLong(context, "amount");
        if (amount <= 0) throw CommandErrors.AMOUNT_MUST_BE_POSITIVE.create();

        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) throw CommandErrors.ACCOUNT_NOT_FOUND.create(player.getName());

        EconomyTransaction transaction = account.increaseBalance(amount);
        if (transaction.isFailure()) throw CommandErrors.TRANSACTION_FAILED.create(transaction.message());

        context.getSource().sendSuccess(() -> Component.literal(
            "Successfully added " + currency.formatValue(amount, false) + " to " + player.getName().getString() + "'s account"
        ).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}