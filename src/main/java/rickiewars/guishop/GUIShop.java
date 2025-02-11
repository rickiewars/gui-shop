package rickiewars.guishop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rickiewars.guishop.config.Config;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.EconomyConfig;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyProvider;
import rickiewars.guishop.sql.DatabaseManager;
import rickiewars.guishop.util.EconomyFileHandler;
import rickiewars.guishop.util.Register;
import rickiewars.guishop.util.ServerHandler;
import rickiewars.guishop.util.ShopFileHandler;

import java.io.IOException;

public class GUIShop implements ModInitializer {
	public static final String MODID = "guishop";
    public static final Logger LOGGER = LoggerFactory.getLogger("gui-shop");

	/**
	 * Holds the shops that are currently loaded
	 */
	public static Config config = new Config();
	public static EconomyConfig economyConfig = new EconomyConfig();

	public static DatabaseManager databaseManager;

	static {
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			try {
				onServerShutdown();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ServerHandler.init(server);
			loadConfig();
		});
	}

	private static void loadConfig() {
		if(!ConfigManager.loadConfig())
			throw new RuntimeException("Could not load config");
	}

	private static void loadEconomyConfig() {
		if(!ConfigManager.loadEconomyConfig())
			throw new RuntimeException("Could not load economy config");
	}

	@Override
	public void onInitialize() {
		LOGGER.info("GUI Shop loaded!");

		loadEconomyConfig();
		Register.registerCommands();
		GuiShopEconomyProvider.init();

		ShopFileHandler fileHandler = new ShopFileHandler();
		if (!fileHandler.initialize()) {
			String msg = "Could not initialize shops-to-file save daemon";
			System.out.println(msg);
			LOGGER.info(msg);
		}
		// TODO: Consider if this is necessary
		EconomyFileHandler ecoFileHandler = new EconomyFileHandler();
		if (!ecoFileHandler.initialize()) {
			String msg = "Could not initialize economy-to-file save daemon";
			System.out.println(msg);
			LOGGER.info(msg);
		}

	}

	public static void onServerShutdown() throws IOException {
		ShopFileHandler fileHandler = new ShopFileHandler();
		fileHandler.saveToFile();
		fileHandler.killTask();
		LOGGER.info("Shops saved to file");

		EconomyFileHandler ecoFileHandler = new EconomyFileHandler();
		ecoFileHandler.saveToFile();
		ecoFileHandler.killTask();
		LOGGER.info("Economy saved to file");
	}
}