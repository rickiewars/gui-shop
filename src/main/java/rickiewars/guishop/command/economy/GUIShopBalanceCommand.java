package rickiewars.guishop.command.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.economy.subcommands.GUIShopBalanceAddCommand;
import rickiewars.guishop.command.economy.subcommands.GUIShopBalanceRemoveCommand;
import rickiewars.guishop.command.economy.subcommands.GUIShopBalanceSendCommand;
import rickiewars.guishop.command.economy.subcommands.GuiShopBalanceCommands;
import rickiewars.guishop.command.suggestions.CurrencySuggestionProvider;

import java.util.Collection;

public class GUIShopBalanceCommand extends GuiShopBalanceCommands {

    public static LiteralArgumentBuilder<ServerCommandSource> getBalanceNode(String literal) {
        return CommandManager.literal(literal)
            .requires(GuiShopPermission.BALANCE.require())
            .executes(GUIShopBalanceCommand::showAllBalances)
            .then(CommandManager.argument("currency", IdentifierArgumentType.identifier())
                .suggests(new CurrencySuggestionProvider())
                .executes(GUIShopBalanceCommand::run)

                .then(GUIShopBalanceAddCommand.register())
                .then(GUIShopBalanceRemoveCommand.register())
                .then(GUIShopBalanceSendCommand.register())
            );
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        if (!GUIShop.economyConfig.economyCommandsEnabled()) return;

        LiteralCommandNode<ServerCommandSource> guishopNode = dispatcher.register(
            CommandManager.literal("guishop").then(getBalanceNode("balance"))
        );

        String alias = GUIShop.economyConfig.command != null ? GUIShop.economyConfig.command.alias : "";
        if (!alias.isEmpty()) {
            if (alias.startsWith("/")) alias = alias.substring(1);

            // Redirect doesn't work correctly for some reason, so I just register the node again under the alias
            dispatcher.register(getBalanceNode(alias));
        }
    }

    private static int showAllBalances(CommandContext<ServerCommandSource> context) {
        if (! (context.getSource().getEntity() instanceof PlayerEntity)) {
            context.getSource().sendFeedback(() -> Text.literal("You must be a player to run this command").formatted(Formatting.RED), false);
            return -1;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();

        Collection<EconomyCurrency> currencies = CommonEconomy.getCurrencies(context.getSource().getServer());
        if (currencies.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("No currencies found").formatted(Formatting.RED), false);
            return -1;
        }
        currencies.forEach(currency -> {
            EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
            if (account == null) return;
            context.getSource().sendFeedback(() -> Text.literal(String.format(
                "Balance for %s: %s",
                currency.id(),
                currency.formatValue(account.balance(), false)
            )).formatted(Formatting.GREEN), false);
        });

        return 0;
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        if (! (context.getSource().getEntity() instanceof PlayerEntity)) {
            context.getSource().sendFeedback(() -> Text.literal("You must be a player to run this command").formatted(Formatting.RED), false);
            return -1;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
        EconomyCurrency currency = getCurrency(context);
        if (currency == null) {
            context.getSource().sendFeedback(() -> Text.literal("Currency not found").formatted(Formatting.RED), false);
            return -1;
        }

        // TODO: Maybe in future allow for multiple accounts per currency. A lot of the groundwork is already done.
        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) {
            context.getSource().sendFeedback(() -> Text.literal("Account not found").formatted(Formatting.RED), false);
            return -1;
        }

        context.getSource().sendFeedback(() -> Text.literal(String.format(
            "Balance for %s: %s",
            currency.id(),
            currency.formatValue(account.balance(), false))
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}