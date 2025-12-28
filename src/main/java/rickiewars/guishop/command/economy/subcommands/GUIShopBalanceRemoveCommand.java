package rickiewars.guishop.command.economy.subcommands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.errors.CommandErrors;

public class GUIShopBalanceRemoveCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("remove")
            .requires(GuiShopPermission.BALANCE_REMOVE.require())
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .then(CommandManager.argument("amount", LongArgumentType.longArg(-1))
                    .executes(GUIShopBalanceRemoveCommand::run)
                ));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        EconomyCurrency currency = GuiShopBalanceCommands.getCurrency(context);

        long amount = LongArgumentType.getLong(context, "amount");
        if (amount <= 0) throw CommandErrors.AMOUNT_MUST_BE_POSITIVE.create();

        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) throw CommandErrors.ACCOUNT_NOT_FOUND.create(player.getName());

        EconomyTransaction transaction = account.decreaseBalance(amount);
        if (transaction.isFailure()) throw CommandErrors.TRANSACTION_FAILED.create(transaction.message());

        context.getSource().sendFeedback(() -> Text.literal(
            "Successfully removed " + currency.formatValue(amount, true) + " from " + player.getName().getString() + "'s account"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}