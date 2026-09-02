package rickiewars.guishop.config;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.shop.SellPricing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiShopConfig {
    public static final String DIR_NAME = "gui-shop";
    public static final String FILE_NAME = "config.json";

    /**
     * Whether the built-in economy is enabled.
     * If true, the economy will be disabled and no economy commands will be available.
     */
    public boolean economyDisabled;

    /**
     * Configure the storage type for the build-in economy.
     * Currently only supports sqlite.
     * Ignored if the economy is disabled through the economyDisabled field.
     */
    @Nullable
    public DatabaseConfig database;

    /**
     * Configure the build-in economy.
     * Currency and account identifiers defined here have a namespace of guishop.
     * Ignored if the economy is disabled through the economyDisabled field.
     */
    @Nullable
    public EconomyProviderDefinition economy;

    /**
     * Configure an in-game command for interacting with the common economy api.
     */
    public CommandConfig command;

    /**
     * Configure default economy or economies provided by other mods
     */
    public EconomyProviders economyProviders;

    /**
     * Default sell-pricing parameters, overridable per shop file.
     */
    public SellPricing sellPricing;

    public GuiShopConfig(
            @Nullable EconomyProviderDefinition economy,
            @Nullable DatabaseConfig database,
            CommandConfig command,
            EconomyProviders economyProviders,
            SellPricing sellPricing
    ) {
        this.economy = economy;
        this.database = database;
        this.command = command;
        this.economyProviders = economyProviders;
        this.sellPricing = sellPricing;
    }

    public GuiShopConfig() {
        this(null, null, new CommandConfig(), new EconomyProviders(), SellPricing.DEFAULT);
    }

    public boolean economyConfigured() {
        return economy != null && economy.isConfigured();
    }

    public boolean economyCommandsEnabled() {
        return !command.disabled;
    }

    public boolean economyProvidersConfigured() {
        return economyProviders != null && !economyProviders.isEmpty();
    }

    public void configureDefaultEconomyProvider() {
        if (economyProviders == null) economyProviders = new EconomyProviders();
        economyProviders.put(
            GuiShopEconomyCurrency.DEFAULT_ID,
            new java.util.LinkedList<>() {{
                add(GuiShopEconomyAccount.DEFAULT_ID.getPath());
            }}
        );
    }

    public void configureDefaultEconomy() {
        if (economy == null) economy = new EconomyProviderDefinition();

        if (!economy.currencies.containsKey(GuiShopEconomyCurrency.DEFAULT_ID.getPath())) {
            economy.currencies.put(GuiShopEconomyCurrency.DEFAULT_ID.getPath(), new CurrencyDefinition(
                    "Credits", "$", "", 2, GuiShopEconomyCurrency.DEFAULT_ICON_ID
            ));
        }
        if (!economy.accounts.containsKey(GuiShopEconomyAccount.DEFAULT_ID.getPath())) {
            economy.accounts.put(GuiShopEconomyAccount.DEFAULT_ID.getPath(), new AccountDefinition(
                    GuiShopEconomyCurrency.DEFAULT_ID.getPath(), "Account", GuiShopEconomyAccount.DEFAULT_ICON_ID
            ));
        }
    }

    public static class EconomyProviders extends HashMap<Identifier, List<String>> {
        public Identifier getFirstCurrency() {
            return this.entrySet().iterator().next().getKey();
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
        public String name;
        public String prefix;
        public String suffix;
        public int decimalPlaces;
        /** Bare item id, resolved to an ItemStack lazily at the GUI boundary. */
        public Identifier icon;

        public CurrencyDefinition(String name, String prefix, String suffix, int decimalPlaces, Identifier icon) {
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
        /** Bare item id, resolved to an ItemStack lazily at the GUI boundary. */
        public Identifier icon;

        public AccountDefinition(String currencyId, String name, Identifier icon) {
            this.currencyId = Identifier.of(GuiShopEconomyProvider.ID, currencyId);
            this.name = name;
            this.icon = icon;
        }
    }

    public static class DatabaseConfig {
        public static final DatabaseType DEFAULT_TYPE = DatabaseType.SQLITE;
        public static final String DEFAULT_FILE_LOCATION = "./world/guishop.sqlite";

        public enum DatabaseType {
            SQLITE("sqlite");

            public final String name;
            DatabaseType(String name) {
                this.name = name;
            }
        }

        public DatabaseType type;
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
