package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;

import java.util.Collection;
import java.util.NoSuchElementException;

public class EconomyUtils {

    public static Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return CommonEconomy.getCurrencies(server);
    }

    public static String getFirstCurrencyId() {
        return GUIShop.config.economyProviders.currencies.keySet().stream().findFirst()
                .orElse(GuiShopEconomyCurrency.DEFAULT_ID);
    }

    private static EconomyProvider getProvider(String currencyId) {
        String providerId = GUIShop.config.economyProviders.currencies.get(currencyId);
        return CommonEconomy.getProvider(providerId);
    }

    public static EconomyAccount getAccount(ServerPlayerEntity player, String currencyId) {
        EconomyProvider economyProvider = EconomyUtils.getProvider(currencyId);
        if (economyProvider == null) {
            throw new NoSuchElementException(
                    "Could not find economy providing this currency"
            );
        }
        EconomyCurrency currency = economyProvider.getCurrency(player.server, currencyId);
        if (currency == null) {
            throw new NoSuchElementException(
                    "Could not find currency"
            );
        }
        return economyProvider.getDefaultAccount(player, currency);
    }
}
