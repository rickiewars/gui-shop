package rickiewars.guishop.config;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyAccount;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyProvider;
import rickiewars.guishop.shop.Shop;

import java.util.LinkedList;

/**
 * An interface representing the configuration structure
 */
public class Config {
    /**
     * Configure the build-in economy.
     * Currency and account identifiers defined here have a namespace of guishop.
     * Omit this field to disable the built-in economy.
     */
    @Nullable
    public EconomyDefinition economy;
    /**
     * Configure default economy or economies provided by other mods
     */
    public EconomyProviders economyProviders;

    public LinkedList<Shop> shops;

    public Config(
            LinkedList<Shop> shops,
            @Nullable EconomyDefinition economy,
            EconomyProviders economyProviders
    ) {
        this.shops = shops;
        this.economy = economy;
        this.economyProviders = economyProviders;
    }
    public Config(LinkedList<Shop> shops){
        this(shops, null, new EconomyProviders());
    }
    public Config(){
        this(new LinkedList<>(), null, new EconomyProviders());
    }

    public boolean economyConfigured() {
        return economy != null && economy.isConfigured();
    }

    public boolean economyProvidersConfigured() {
        return economyProviders != null && economyProviders.isConfigured();
    }

    public void configureDefaultEconomy() {
        if (economy == null) economy = new EconomyDefinition();

        if (!economy.currencies.containsKey(GuiShopEconomyCurrency.DEFAULT_ID)) {
            economy.currencies.put(GuiShopEconomyCurrency.DEFAULT_ID, new CurrencyDefinition(
                    "Credits", "$", "", new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
            ));
        }
        if (!economy.accounts.containsKey(GuiShopEconomyAccount.DEFAULT_ID)) {
            economy.accounts.put(GuiShopEconomyAccount.DEFAULT_ID, new AccountDefinition(
                    GuiShopEconomyCurrency.DEFAULT_ID, "Account", new ItemStack(GuiShopEconomyAccount.DEFAULT_ICON)
            ));
        }
    }

    public void configureDefaultEconomyProvider() {
        economyProviders.currencies.put(GuiShopEconomyCurrency.DEFAULT_ID, GuiShopEconomyProvider.ID);
        economyProviders.accounts.put(GuiShopEconomyAccount.DEFAULT_ID, GuiShopEconomyCurrency.DEFAULT_ID);
    }

    public static class EconomyDefinition extends BaseEconomyConfig<CurrencyDefinition,AccountDefinition> {}
    public static class EconomyProviders extends BaseEconomyConfig<String, String> {}

    public static class CurrencyDefinition {
        // The name of the currency
        public String name;
        // The prefix to display before the currency value
        public String prefix;
        // The suffix to display after the currency value
        public String suffix;
        // The icon to display for the currency, used in the GUI
        public ItemStack icon;

        public CurrencyDefinition(String name, String prefix, String suffix, ItemStack icon) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.icon = icon;
        }
    }

    public static class AccountDefinition {
        public String currencyId;
        public String name;
        public ItemStack icon;
        public AccountDefinition(String currencyId, String name, ItemStack icon) {
            this.currencyId = currencyId;
            this.name = name;
            this.icon = icon;
        }
    }

}
