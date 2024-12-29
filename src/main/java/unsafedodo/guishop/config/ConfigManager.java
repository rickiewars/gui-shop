package unsafedodo.guishop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.economy.EconomyType;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;
import unsafedodo.guishop.util.ShopItemSerializer;
import unsafedodo.guishop.util.ShopSerializer;

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
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Initialize a new empty configuration file.
     * Only gets called if the configuration file does not exist on load.
     */
    private static ConfigData initConfigFile(File configFile) throws IOException {
        ConfigData configData = new ConfigData();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        writer.write(GSON.toJson(configData));
        writer.close();

        return configData;
    }

    /**
     * Try to load the configuration data from guishop.json
     */
    private static ConfigData getConfigData(File configFile) throws IOException {
        return configFile.exists() ? GSON.fromJson(
            new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8),
            ConfigData.class
        ) : initConfigFile(configFile);
    }

    /**
     * Load the configuration data from guishop.json
     * @return true if successful, false otherwise
     */
    public static boolean loadConfig(){
        boolean success;
        try {
            File configDir = Paths.get("", "config").toFile();
            File configFile = new File(configDir, "guishop.json");

            ConfigData configData = getConfigData(configFile);

            if (configData.economyType == null) {
                GUIShop.LOGGER.warn("Unknown economy type. Falling back to next supported economy.");
                configData.economyType = EconomyType.firstLoaded();
            }
            if (configData.economyType == null) {
                GUIShop.LOGGER.error("None of the supported economy mods are found. Please install at least one.");
                return false;
            }
            if (configData.economyType != EconomyType.MOCK && !configData.economyType.modIsLoaded()) {
                GUIShop.LOGGER.error(
                    "Configured economy type " +
                    configData.economyType.pretty() +
                    " is not loaded. Make sure the mod you configured is installed."
                );
                return false;
            }
            GUIShop.LOGGER.info("Configured economy: " + configData.economyType.pretty());
            GUIShop.economyService = configData.economyType.getEconomyService();

            GUIShop.shops.clear();
            if(configData.shops != null){
                for(Shop shop: configData.shops)
                    GUIShop.shops.addLast(shop);
            }


            success = true;

        } catch (IOException e){
            success = false;
        }

        return success;
    }
}
