package rickiewars.guishop.api.economy.impl;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.GuiShopConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GuiShopEconomyProvider implements EconomyProvider {
    public static final String ID = GUIShop.MODID;
    public static final GuiShopEconomyProvider INSTANCE = new GuiShopEconomyProvider();


    public static void init() {
        CommonEconomy.register(ID, INSTANCE);
    }

    @Override
    public Component name() {
        return Component.literal("GuiShop Economy");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer minecraftServer, GameProfile gameProfile, String accountId) {
        if (GUIShop.config.economy == null || GUIShop.config.economyDisabled) return null;
        if (!GUIShop.config.economy.accounts.containsKey(accountId)) return null;

        return new GuiShopEconomyAccount(
                Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, accountId),
                GUIShop.config.economy.accounts.get(accountId),
                gameProfile.id()
        );
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer minecraftServer, GameProfile gameProfile) {
        if (GUIShop.config.economy == null || GUIShop.config.economyDisabled) return Collections.emptySet();
        if (GUIShop.config.economy.accounts.isEmpty()) return Collections.emptySet();

        Collection<EconomyAccount> accounts = new ArrayList<>();
        GUIShop.config.economy.accounts.forEach(
            (accountId, accountDefinition) -> accounts.add(new GuiShopEconomyAccount(
                Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, accountId),
                accountDefinition, gameProfile.id()
            )));

        return accounts;
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer minecraftServer, String currencyId) {
        if (GUIShop.config.economy == null || GUIShop.config.economyDisabled) return null;
        if (!GUIShop.config.economy.currencies.containsKey(currencyId)) return null;

        return new GuiShopEconomyCurrency(
                Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, currencyId),
                GUIShop.config.economy.currencies.get(currencyId)
        );
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer minecraftServer) {
        if (GUIShop.config.economy == null || GUIShop.config.economyDisabled) return Collections.emptySet();
        if (GUIShop.config.economy.currencies.isEmpty()) return Collections.emptySet();

        Collection<EconomyCurrency> currencies = new ArrayList<>();
        GUIShop.config.economy.currencies.forEach(
            (currencyId, currencyDefinition) -> currencies.add(new GuiShopEconomyCurrency(
                Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, currencyId),
                currencyDefinition
            )));

        return currencies;
    }

    // Only one account should exist for a currency
    // To keep compatibility with other mods, we will return the first found account
    // Maybe in the future, I'll add a command for the player to choose a default account for a specific currency
    @Override
    public @Nullable String defaultAccount(MinecraftServer minecraftServer, GameProfile gameProfile, EconomyCurrency economyCurrency) {
        if (GUIShop.config.economy == null || GUIShop.config.economyDisabled) return null;
        for (Map.Entry<String, GuiShopConfig.AccountDefinition> entry : GUIShop.config.economy.accounts.entrySet()) {
            if (entry.getValue().currencyId.equals(economyCurrency.id())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public ItemStack icon() {
        return Items.CHEST.getDefaultInstance();
    }
}
