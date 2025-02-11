package rickiewars.guishop.config;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyAccount;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * An interface representing the configuration structure
 */
public class EconomyConfig {
    public static final String FILE_NAME = "guishopeconomy.json";

    /**
     * Whether the economy is enabled.
     * If false, the economy will be disabled and no economy commands will be available.
     */
    public boolean disabled;

    /**
     * Configure the storage type for the build-in economy.
     * Currently only supports sqlite.
     * Ignored if the economy is disabled through the disabled field.
     */
    @Nullable
    public DatabaseConfig database;

    /**
     * Configure the build-in economy.
     * Currency and account identifiers defined here have a namespace of guishop.
     * Ignored if the economy is disabled through the disabled field.
     */
    @Nullable
    public EconomyProviderDefinition economy;

    /**
     * Configure an in-game command for interacting with the common economy api.
     * This can also be used with external economy providers.
     * Will add a "balance" command as a subcommand of guishop.
     * When specifying an alias, the command will be available as main command under the specified name (e.g. "/myAlias").
     */
    public CommandConfig command;

    public EconomyConfig(
            @Nullable EconomyProviderDefinition economy,
            @Nullable DatabaseConfig database,
            CommandConfig command
    ) {
        this.economy = economy;
        this.database = database;
        this.command = command;
    }
    public EconomyConfig(){
        this(null, null, new CommandConfig());
    }

    public boolean economyConfigured() {
        return economy != null && economy.isConfigured();
    }

    public boolean economyCommandsEnabled() {
        return !command.disabled;
    }

    public void configureDefaultEconomy() {
        if (economy == null) economy = new EconomyProviderDefinition();

        if (!economy.currencies.containsKey(GuiShopEconomyCurrency.DEFAULT_ID.getPath())) {
            economy.currencies.put(GuiShopEconomyCurrency.DEFAULT_ID.getPath(), new CurrencyDefinition(
                    "Credits", "$", "", 2, new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
            ));
        }
        if (!economy.accounts.containsKey(GuiShopEconomyAccount.DEFAULT_ID.getPath())) {
            economy.accounts.put(GuiShopEconomyAccount.DEFAULT_ID.getPath(), new AccountDefinition(
                    GuiShopEconomyCurrency.DEFAULT_ID.getPath(), "Account", new ItemStack(GuiShopEconomyAccount.DEFAULT_ICON)
            ));
        }
    }

    public static class CommandConfig {
        public boolean disabled;
        public String alias;

        public CommandConfig(boolean disabled, String alias) {
            this.disabled = disabled;
            this.alias = alias;
        }
        public CommandConfig() {
            this(false, "");
        }
    }

    public static class EconomyProviderDefinition {
        public Map<String, CurrencyDefinition> currencies;
        public Map<String, AccountDefinition> accounts;

        public EconomyProviderDefinition(Map<String, CurrencyDefinition> currencies, Map<String, AccountDefinition> accounts) {
            this.currencies = currencies;
            this.accounts = accounts;
        }
        public EconomyProviderDefinition() {
            this(new HashMap<>(), new HashMap<>());
        }

        public boolean isConfigured() {
            return !currencies.isEmpty() && !accounts.isEmpty();
        }

    }

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
        public Identifier currencyId;
        public String name;
        public ItemStack icon;
        public AccountDefinition(String currencyId, String name, ItemStack icon) {
            this.currencyId = Identifier.of(GuiShopEconomyProvider.ID, currencyId);
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
