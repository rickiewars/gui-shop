package rickiewars.guishop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.impl.SQLiteDatabaseManager;
import rickiewars.guishop.serializer.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Load and hold the merged gui-shop configuration data at config/gui-shop/config.json. Shop
 * items are not part of this file -- see SnbtShopStore.
 */
public class ConfigManager {
    /**
     * Gson instance which holds the configuration data
     */
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(GuiShopConfig.DatabaseConfig.class, new DatabaseConfigSerializer())
        .registerTypeAdapter(GuiShopConfig.EconomyProviderDefinition.class, new EconomyProviderDefinitionSerializer())
        .registerTypeAdapter(GuiShopConfig.CurrencyDefinition.class, new CurrencyDefinitionSerializer())
        .registerTypeAdapter(GuiShopConfig.AccountDefinition.class, new AccountDefinitionSerializer())
        .registerTypeAdapter(GuiShopConfig.CommandConfig.class, new CommandConfigSerializer())
        .registerTypeAdapter(GuiShopConfig.EconomyProviders.class, new EconomyProvidersSerializer())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    public static Path configRoot() {
        return Paths.get("", "config");
    }

    public static Path guiShopConfigDir() {
        return configRoot().resolve(GuiShopConfig.DIR_NAME);
    }

    public static Path guiShopConfigFile() {
        return guiShopConfigDir().resolve(GuiShopConfig.FILE_NAME);
    }

    public static Path shopsDir() {
        return guiShopConfigDir().resolve("shops");
    }

    /**
     * Initialize a new empty configuration file.
     * Only gets called if the configuration file does not exist on load.
     */
    private static GuiShopConfig initConfigFile(File configFile) throws IOException {
        GuiShopConfig config = new GuiShopConfig();
        config.configureDefaultEconomyProvider();

        writeConfig(configFile, config);
        return config;
    }

    private static void writeConfig(File configFile, GuiShopConfig config) throws IOException {
        File parent = configFile.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
            writer.write(GSON.toJson(config));
        }
    }

    /**
     * Try to load the configuration data from config/gui-shop/config.json
     */
    private static GuiShopConfig getConfigData(File configFile) throws IOException {
        return configFile.exists() ? GSON.fromJson(
            new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8),
            GuiShopConfig.class
        ) : initConfigFile(configFile);
    }

    /**
     * Load the configuration from config/gui-shop/config.json
     */
    public static void loadConfig() throws IOException {
        File configFile = guiShopConfigFile().toFile();

        GUIShop.LOGGER.info("Loading config");
        GuiShopConfig config = getConfigData(configFile);
        GUIShop.LOGGER.info("Config loaded");

        boolean configUpdated = false;
        if (!config.economyProvidersConfigured()) {
            configUpdated = true;
            GUIShop.LOGGER.info(
                    "No economy providers have been configured. Adding the built-in economy provider."
            );
            config.configureDefaultEconomyProvider();
        }

        if (!config.economyDisabled) {
            if (!config.economyConfigured()) {
                GUIShop.LOGGER.info(
                        "The built-in economy provider has not been configured. Using default configuration."
                );
                config.configureDefaultEconomy();
                configUpdated = true;
            }

            if (config.database == null) {
                configUpdated = true;
                GUIShop.LOGGER.info(
                        "No database configuration found. Adding the default SQLite configuration."
                );
                config.database = new GuiShopConfig.DatabaseConfig();
            }

            GuiShopConfig.DatabaseConfig.DatabaseType type = config.database.type;
            if (type == GuiShopConfig.DatabaseConfig.DatabaseType.SQLITE) {
                GUIShop.databaseManager = new SQLiteDatabaseManager(config);
            } else {
                throw new RuntimeException("Unsupported database type: " + type);
            }
        }

        GUIShop.config = config;

        if (configUpdated) {
            saveConfig();
        }
    }

    public static void saveConfig() throws IOException {
        writeConfig(guiShopConfigFile().toFile(), GUIShop.config);
    }
}
