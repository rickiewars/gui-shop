package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;

import java.util.Collection;
import java.util.NoSuchElementException;

public class EconomyUtils {

    public static Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return CommonEconomy.getCurrencies(server);
    }

    public static Identifier getFirstCurrencyId() {
        return GUIShop.config.economyProviders.isEmpty()
            ? GuiShopEconomyCurrency.DEFAULT_ID
            : GUIShop.config.economyProviders.getFirstCurrency();
    }

    public static EconomyAccount getDefaultAccount(ServerPlayerEntity player, Identifier currencyId) {
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
