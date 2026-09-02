package rickiewars.guishop.command.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
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
import rickiewars.guishop.errors.CommandErrors;

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
        if (!GUIShop.config.economyCommandsEnabled()) return;

        dispatcher.register(
            CommandManager.literal("guishop").then(getBalanceNode("balance"))
        );

        String alias = GUIShop.config.command != null ? GUIShop.config.command.alias : "";
        if (!alias.isEmpty()) {
            if (alias.startsWith("/")) alias = alias.substring(1);

            // Redirect doesn't work correctly for some reason, so I just register the node again under the alias
            dispatcher.register(getBalanceNode(alias));
        }
    }

    private static int showAllBalances(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        Collection<EconomyCurrency> currencies = CommonEconomy.getCurrencies(context.getSource().getServer());
        if (currencies.isEmpty()) throw CommandErrors.NO_CURRENCIES.create();

        currencies.forEach(currency -> {
            EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
            if (account == null) return;
            context.getSource().sendFeedback(() -> Text.literal(String.format(
                "Balance for %s: %s",
                currency.name(),
                currency.formatValue(account.balance(), false)
            )).formatted(Formatting.GREEN), false);
        });

        return 0;
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        EconomyCurrency currency = GuiShopBalanceCommands.getCurrency(context);
        // TODO: Maybe in future allow for multiple accounts per currency. A lot of the groundwork is already done.
        EconomyAccount account = currency.provider().getDefaultAccount(player, currency);
        if (account == null) throw CommandErrors.ACCOUNT_NOT_FOUND.create(player.getName());

        context.getSource().sendFeedback(() -> Text.literal(String.format(
            "Balance for %s: %s",
            currency.name(),
            currency.formatValue(account.balance(), false))
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}