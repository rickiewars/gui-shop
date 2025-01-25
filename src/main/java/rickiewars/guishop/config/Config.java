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

    public DatabaseConfig database;

    /**
     * Configure the build-in economy.
     * Currency and account identifiers defined here have a namespace of guishop.
     * Omit this field to disable the built-in economy.
     */
    @Nullable
    public EconomyConfig economy;
    /**
     * Configure default economy or economies provided by other mods
     */
    public EconomyProviders economyProviders;

    public LinkedList<Shop> shops;

    public Config(
            LinkedList<Shop> shops,
            @Nullable EconomyConfig economy,
            EconomyProviders economyProviders,
            DatabaseConfig database
    ) {
        this.shops = shops;
        this.economy = economy;
        this.economyProviders = economyProviders;
        this.database = database;
    }
    public Config(LinkedList<Shop> shops){
        this(shops, null, new EconomyProviders(), new DatabaseConfig());
    }
    public Config(){
        this(new LinkedList<>(), null, new EconomyProviders(), new DatabaseConfig());
    }

    public boolean economyConfigured() {
        return economy != null && economy.isConfigured();
    }

    public boolean economyProvidersConfigured() {
        return economyProviders != null && economyProviders.isConfigured();
    }

    public void configureDefaultEconomy() {
        if (economy == null) economy = new EconomyConfig();

        if (!economy.currencies.containsKey(GuiShopEconomyCurrency.DEFAULT_ID)) {
            economy.currencies.put(GuiShopEconomyCurrency.DEFAULT_ID, new CurrencyDefinition(
                    "Credits", "$", "", 2, new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
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

    public static class EconomyConfig extends BaseEconomyConfig<CurrencyDefinition,AccountDefinition> {}
    public static class EconomyProviders extends BaseEconomyConfig<String, String> {}

    public static class CurrencyDefinition {
        // The name of the currency
        public String name;
        // The prefix to display before the currency value
        public String prefix;
        // The suffix to display after the currency value
        public String suffix;
        // The number of decimal places to display
        public int decimalPlaces;
        // The icon to display for the currency, used in the GUI
        public ItemStack icon;

        public CurrencyDefinition(String name, String prefix, String suffix, int decimalPlaces, ItemStack icon) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.decimalPlaces = decimalPlaces;
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

    public static class DatabaseConfig {
        public static final DatabaseType DEFAULT_TYPE = DatabaseType.SQLITE;
        public static final String DEFAULT_FILE_LOCATION = "./config/guishop.sqlite";

        public enum DatabaseType {
            SQLITE("sqlite");

            public final String name;
            DatabaseType(String name) {
                this.name = name;
            }
        }

        public DatabaseType type;
        // Use null to use the default location
        // Only used if type is sqlite
        // Example: "./config/guishop.sqlite"
        public String fileLocation;

        public DatabaseConfig(DatabaseType type, String fileLocation) {
            this.type = type;
            this.fileLocation = fileLocation;
        }

        public DatabaseConfig() {
            this(DEFAULT_TYPE, DEFAULT_FILE_LOCATION);
        }

    }

}
