package rickiewars.guishop.command.economy.subcommands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.command.GuiShopPermission;

public class GUIShopBalanceSendCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("send")
            .requires(GuiShopPermission.BALANCE_SEND.require())
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .then(CommandManager.argument("amount", LongArgumentType.longArg(-1))
                    .executes(GUIShopBalanceSendCommand::run)
                ));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (! (context.getSource().getEntity() instanceof PlayerEntity)) {
            context.getSource().sendFeedback(() -> Text.literal(
                "You must be a player to run this command"
            ).formatted(Formatting.RED), false);
            return -1;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");

        if (player == targetPlayer) {
            context.getSource().sendFeedback(() -> Text.literal(
                "You can't send money to yourself"
            ).formatted(Formatting.RED), false);
            return -1;
        }

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

        EconomyAccount senderAccount = currency.provider().getDefaultAccount(player, currency);
        if (senderAccount == null) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Account of " + player.getName().toString() + " not found"
            ).formatted(Formatting.RED), false);
            return -1;
        }

        EconomyAccount receiverAccount = currency.provider().getDefaultAccount(targetPlayer, currency);
        if (receiverAccount == null) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Account of " + targetPlayer.getName().toString() + " not found"
            ).formatted(Formatting.RED), false);
            return -1;
        }

        EconomyTransaction transaction = senderAccount.canDecreaseBalance(amount);
        if (transaction.isFailure()) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Failed to remove balance: " + senderAccount.canDecreaseBalance(amount).message()
            ).formatted(Formatting.RED), false);
            return -1;
        }

        if (receiverAccount.canIncreaseBalance(amount).isFailure()) {
            context.getSource().sendFeedback(() -> Text.literal(
                "Could not send the amount to " + targetPlayer.getName().toString()
            ).formatted(Formatting.RED), false);
            return -1;
        }

        senderAccount.decreaseBalance(amount);
        receiverAccount.increaseBalance(amount);

        context.getSource().sendFeedback(() -> Text.literal(
            "Successfully sent " + currency.formatValue(amount, false) + " to " + targetPlayer.getName().getString() + "'s account"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}