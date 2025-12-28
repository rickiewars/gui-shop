package rickiewars.guishop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.impl.SQLiteDatabaseManager;
import rickiewars.guishop.serializer.*;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.EconomyFileHandler;
import rickiewars.guishop.util.ShopFileHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Load and hold the configuration data for the plugin
 */
public class ConfigManager {
    /**
     * Gson instance which holds the configuration data
     */
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(ShopItem.class, new ShopItemSerializer())
        .registerTypeAdapter(Shop.class, new ShopSerializer())
        .registerTypeAdapter(EconomyConfig.DatabaseConfig.class, new DatabaseConfigSerializer())
        .registerTypeAdapter(EconomyConfig.EconomyProviderDefinition.class, new EconomyProviderDefinitionSerializer())
        .registerTypeAdapter(EconomyConfig.CurrencyDefinition.class, new CurrencyDefinitionSerializer())
        .registerTypeAdapter(EconomyConfig.AccountDefinition.class, new AccountDefinitionSerializer())
        .registerTypeAdapter(EconomyConfig.CommandConfig.class, new CommandConfigSerializer())
        .registerTypeAdapter(Config.EconomyProviders.class, new EconomyProvidersSerializer())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    /**
     * Initialize a new empty configuration file.
     * Only gets called if the configuration file does not exist on load.
     */
    private static Config initConfigFile(File configFile) throws IOException {
        Config config = new Config();
        config.configureDefaultEconomyProvider();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        writer.write(GSON.toJson(config));
        writer.close();

        return config;
    }

    /**
     * Initialize a new empty economy configuration file.
     * Only gets called if the economy configuration file does not exist on load.
     */
    private static EconomyConfig initEconomyConfigFile(File configFile) throws IOException {
        EconomyConfig econConfig = new EconomyConfig();
        econConfig.configureDefaultEconomy();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        writer.write(GSON.toJson(econConfig));
        writer.close();

        return econConfig;
    }

    /**
     * Try to load the configuration data from guishop.json
     */
    private static Config getConfigData(File configFile) throws IOException {
        return configFile.exists() ? GSON.fromJson(
            new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8),
            Config.class
        ) : initConfigFile(configFile);
    }

    /**
     * Try to load the configuration data from guishopeconomy.json
     */
    private static EconomyConfig getEconomyConfigData(File configFile) throws IOException {
        return configFile.exists() ? GSON.fromJson(
            new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8),
            EconomyConfig.class
        ) : initEconomyConfigFile(configFile);
    }

    /**
     * Load the configuration data from guishop.json
     */
    public static void loadConfig() throws IOException {
        boolean configUpdated = false;

        File configDir = Paths.get("", "config").toFile();
        File configFile = new File(configDir, Config.FILE_NAME);

        GUIShop.LOGGER.info("Loading config");
        Config config = getConfigData(configFile);
        GUIShop.LOGGER.info("Config loaded");

        if (!config.economyProvidersConfigured()) {
            configUpdated = true;
            GUIShop.LOGGER.info(
                    "No economy providers have been configured. Adding the built-in economy provider."
            );
            config.configureDefaultEconomyProvider();
        }

        GUIShop.config = config;

        if (configUpdated) {
            ShopFileHandler fileHandler = new ShopFileHandler();
            fileHandler.saveToFile();
        }
    }

    /**
     * load the economy configuration data from guishopeconomy.json
     * @return true if successful, false otherwise
     */
    public static boolean loadEconomyConfig() {
        boolean success;
        boolean configUpdated = false;
        try {
            File configDir = Paths.get("", "config").toFile();
            File configFile = new File(configDir, EconomyConfig.FILE_NAME);

            GUIShop.LOGGER.info("Loading economy config");
            EconomyConfig econConfig = getEconomyConfigData(configFile);
            GUIShop.LOGGER.info("Economy config loaded");

            if (!econConfig.economyConfigured() && !econConfig.disabled) {
                GUIShop.LOGGER.info(
                        "The built-in economy provider has not been configured. Using default configuration."
                );
                econConfig.configureDefaultEconomy();
            }

            if (econConfig.database == null && !econConfig.disabled) {
                configUpdated = true;
                GUIShop.LOGGER.info(
                        "No database configuration found. Adding the default SQLite configuration."
                );
                econConfig.database = new EconomyConfig.DatabaseConfig();
            }

            if (!econConfig.disabled) {
                EconomyConfig.DatabaseConfig.DatabaseType type = econConfig.database.type;
                if (type == EconomyConfig.DatabaseConfig.DatabaseType.SQLITE) {
                    GUIShop.databaseManager = new SQLiteDatabaseManager(econConfig);
                } else {
                    throw new RuntimeException("Unsupported database type: " + type);
                }
            }

            GUIShop.economyConfig = econConfig;

            if (configUpdated) {
                EconomyFileHandler fileHandler = new EconomyFileHandler();
                fileHandler.saveToFile();
            }

            success = true;

        } catch (IOException e) {
            success = false;
        }

        return success;
    }
}
