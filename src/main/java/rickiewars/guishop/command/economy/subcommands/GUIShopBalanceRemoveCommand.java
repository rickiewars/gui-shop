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
        if (currency == null) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Currency not found"
            ).formatted(Formatting.RED), false);
            return -1;
        }

        long amount = LongArgumentType.getLong(context, "amount");
        if (amount <= 0) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Amount must be greater than 0"
            ).formatted(Formatting.RED), false);
            return -1;
        }

        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Account not found"
            ).formatted(Formatting.RED), false);
            return -1;
        }

        EconomyTransaction transaction = account.decreaseBalance(amount);
        if (transaction.isFailure()) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Failed to remove balance: " + transaction.message()
            ).formatted(Formatting.RED), false);
            return -1;
        }

        context.getSource().sendFeedback(() -> Text.literal(
            "Successfully removed " + currency.formatValue(amount, true) + " from " + player.getName().getString() + "'s account"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}