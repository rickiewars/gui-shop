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
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
        if (player == targetPlayer) throw CommandErrors.TARGET_SELF.create();

        EconomyCurrency currency = GuiShopBalanceCommands.getCurrency(context);

        long amount = LongArgumentType.getLong(context, "amount");
        if (amount <= 0) throw CommandErrors.AMOUNT_MUST_BE_POSITIVE.create();

        EconomyAccount senderAccount = currency.provider().getDefaultAccount(player, currency);
        if (senderAccount == null) throw CommandErrors.ACCOUNT_NOT_FOUND.create(player.getName());

        EconomyAccount receiverAccount = currency.provider().getDefaultAccount(targetPlayer, currency);
        if (receiverAccount == null) throw CommandErrors.ACCOUNT_NOT_FOUND.create(targetPlayer.getName());

        EconomyTransaction decreaseTransaction = senderAccount.canDecreaseBalance(amount);
        if (decreaseTransaction.isFailure()) throw CommandErrors.TRANSACTION_FAILED.create(decreaseTransaction.message());

        EconomyTransaction increaseTransaction = receiverAccount.canIncreaseBalance(amount);
        if (increaseTransaction.isFailure()) throw CommandErrors.TRANSACTION_FAILED.create(increaseTransaction.message());

        senderAccount.decreaseBalance(amount);
        receiverAccount.increaseBalance(amount);

        context.getSource().sendFeedback(() -> Text.literal(
            "Successfully sent " + currency.formatValue(amount, false) + " to " + targetPlayer.getName().getString() + "'s account"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}