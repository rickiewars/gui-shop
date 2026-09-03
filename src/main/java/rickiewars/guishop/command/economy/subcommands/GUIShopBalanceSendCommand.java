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

public class GUIShopBalanceSendCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("send")
            .requires(GuiShopPermission.BALANCE_SEND.require())
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", LongArgumentType.longArg(-1))
                    .executes(GUIShopBalanceSendCommand::run)
                ));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
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

        context.getSource().sendSuccess(() -> Component.literal(
            "Successfully sent " + currency.formatValue(amount, false) + " to " + targetPlayer.getName().getString() + "'s account"
        ).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}