package rickiewars.guishop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.serializer.*;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.sql.SQLiteDatabaseManager;
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
            .registerTypeAdapter(Config.DatabaseConfig.class, new DatabaseConfigSerializer())
            .registerTypeAdapter(Config.EconomyConfig.class, new EconomyConfigSerializer())
            .registerTypeAdapter(Config.CurrencyDefinition.class, new CurrencyDefinitionSerializer())
            .registerTypeAdapter(Config.AccountDefinition.class, new AccountDefinitionSerializer())
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
        config.configureDefaultEconomy();
        config.configureDefaultEconomyProvider();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        writer.write(GSON.toJson(config));
        writer.close();

        return config;
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
     * Load the configuration data from guishop.json
     * @return true if successful, false otherwise
     */
    public static boolean loadConfig(){
        boolean success;
        boolean configUpdated = false;
        try {
            File configDir = Paths.get("", "config").toFile();
            File configFile = new File(configDir, "guishop.json");

            Config config = getConfigData(configFile);

            if (!config.economyProvidersConfigured()) {
                configUpdated = true;
                GUIShop.LOGGER.info(
                        "No economy providers have been configured. Adding the built-in economy provider."
                );
                config.configureDefaultEconomyProvider();
                if (!config.economyConfigured()) {
                    GUIShop.LOGGER.info(
                            "The built-in economy provider has not been configured. Using default configuration."
                    );
                    config.configureDefaultEconomy();
                }
            }

            if (config.database == null) {
                configUpdated = true;
                GUIShop.LOGGER.info(
                        "No database configuration found. Adding the default SQLite configuration."
                );
                config.database = new Config.DatabaseConfig();
            }

            Config.DatabaseConfig.DatabaseType type = config.database.type;
            if (type == Config.DatabaseConfig.DatabaseType.SQLITE) {
                SQLiteDatabaseManager.initDatabase(config);
                GUIShop.databaseManager = new SQLiteDatabaseManager();
            } else {
                throw new RuntimeException("Unsupported database type: " + type);
            }


            GUIShop.config = config;

            if (configUpdated) {
                ShopFileHandler fileHandler = new ShopFileHandler();
                fileHandler.saveToFile();
            }

            success = true;

        } catch (IOException e){
            success = false;
        }

        return success;
    }
}
