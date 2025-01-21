package rickiewars.guishop.economy.economyProvider;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;

import java.util.*;

public class GuiShopEconomyProvider implements EconomyProvider {
    public static final String ID = GUIShop.MODID;
    public static final GuiShopEconomyProvider INSTANCE = new GuiShopEconomyProvider();


    public static void init() {
        CommonEconomy.register(ID, INSTANCE);
    }

    @Override
    public Text name() {
        return Text.literal("GuiShop Economy");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer minecraftServer, GameProfile gameProfile, String accountId) {
        if (GUIShop.config.economy == null) return null;
        if (!GUIShop.config.economy.accounts.containsKey(accountId)) return null;

        return new GuiShopEconomyAccount(
                Identifier.of(accountId),
                GUIShop.config.economy.accounts.get(accountId),
                gameProfile.getId()
        );
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer minecraftServer, GameProfile gameProfile) {
        if (GUIShop.config.economy == null) return Collections.emptySet();
        if (GUIShop.config.economy.accounts.isEmpty()) return Collections.emptySet();

        Collection<EconomyAccount> accounts = new ArrayList<>();
        GUIShop.config.economy.accounts.forEach((accountId, accountDefinition) -> {
            accounts.add(new GuiShopEconomyAccount(Identifier.of(accountId), accountDefinition, gameProfile.getId()));
        });

        return accounts;
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer minecraftServer, String currencyId) {
        if (GUIShop.config.economy == null) return null;
        if (!GUIShop.config.economy.currencies.containsKey(currencyId)) return null;

        return new GuiShopEconomyCurrency(
                Identifier.of(currencyId),
                GUIShop.config.economy.currencies.get(currencyId)
        );
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer minecraftServer) {
        if (GUIShop.config.economy == null) return Collections.emptySet();
        if (GUIShop.config.economy.currencies.isEmpty()) return Collections.emptySet();

        Collection<EconomyCurrency> currencies = new ArrayList<>();
        GUIShop.config.economy.currencies.forEach((currencyId, currencyDefinition) -> {
            currencies.add(new GuiShopEconomyCurrency(Identifier.of(currencyId), currencyDefinition));
        });

        return currencies;
    }

    // Only one account should exist for a currency
    // To keep compatibility with other mods, we will return the first found account
    // Maybe in the future, I'll add a command for the player to choose a default account for a specific currency
    @Override
    public @Nullable String defaultAccount(MinecraftServer minecraftServer, GameProfile gameProfile, EconomyCurrency economyCurrency) {
        if (GUIShop.config.economy == null) return null;
        Optional<Map.Entry<String, Config.AccountDefinition>> account = GUIShop.config.economy.accounts.entrySet().stream()
                .filter(entry -> entry.getValue().currencyId.equals(economyCurrency.id().toString())).findFirst();

        return account.map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public ItemStack icon() {
        return Items.CHEST.getDefaultStack();
    }
}
