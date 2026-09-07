package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;

import java.util.Collection;
import java.util.NoSuchElementException;

public class EconomyUtils {

    public static Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return CommonEconomy.getCurrencies(server);
    }

    /** Registers (or refreshes) a player's account for every currency. */
    public static void registerAccounts(DatabaseManager databaseManager, Collection<EconomyCurrency> currencies, String uuid, String name) {
        currencies.forEach(currency -> databaseManager.updateAccount(currency.id().toString(), uuid, name));
    }

    public static Identifier getFirstCurrencyId() {
        return GUIShop.config.economyProviders.isEmpty()
            ? GuiShopEconomyCurrency.DEFAULT_ID
            : GUIShop.config.economyProviders.getFirstCurrency();
    }

    public static EconomyAccount getDefaultAccount(ServerPlayer player, Identifier currencyId) {
        EconomyCurrency currency = CommonEconomy.getCurrency(
            GUIShop.minecraftServer.getInstance(),
            currencyId
        );
        if (currency == null) {
            throw new NoSuchElementException(
                    "Could not find currency"
            );
        }
        return currency.provider().getDefaultAccount(player, currency);
    }
}
