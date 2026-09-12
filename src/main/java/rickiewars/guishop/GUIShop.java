package rickiewars.guishop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.database.DatabaseManagerFactory;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.IServer;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.api.minecraft.impl.MinecraftServer;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig;
import rickiewars.guishop.migration.LegacyConfigMigrator;
import rickiewars.guishop.migration.LegacyMigrationCleanup;
import rickiewars.guishop.migration.LegacyShopConverter;
import rickiewars.guishop.serializer.SnbtShopStore;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.Register;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GUIShop implements ModInitializer {
	public static final String MODID = "guishop";
    public static final Logger LOGGER = LoggerFactory.getLogger("gui-shop");

	public static GuiShopConfig config = new GuiShopConfig();

	/**
	 * Holds the shops that are currently loaded. Populated at SERVER_STARTED, once dynamic
	 * registries (needed to decode item components, e.g. enchantments) are available.
	 */
	public static List<Shop> shops = new LinkedList<>();

	public static SnbtShopStore shopStore;

	public static DatabaseManager databaseManager;

	public static IServer minecraftServer;

	static {
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> onServerShutdown());
		ServerLifecycleEvents.SERVER_STARTING.register(GUIShop::onServerStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(GUIShop::onServerStarted);
	}

	private static void loadConfig() {
		try {
			ConfigManager.loadConfig();
		} catch (IOException e) {
			throw new RuntimeException("Could not load config file", e);
		}
	}

	private static void onServerStarting(net.minecraft.server.MinecraftServer server) {
		minecraftServer = new MinecraftServer(server);

		if (!config.economyDisabled) {
			databaseManager = DatabaseManagerFactory.create(config);
		}
	}

	private static void onServerStarted(net.minecraft.server.MinecraftServer server) {
		MinecraftItemCodec itemCodec = new MinecraftItemCodec(server);

		boolean configMigrationOk = LegacyConfigMigrator.migrateIfNeeded();
		boolean shopConversionOk = LegacyShopConverter.convertIfNeeded(server, itemCodec);
		LegacyMigrationCleanup.cleanupIfComplete(configMigrationOk, shopConversionOk);

		if (!shopConversionOk) {
			LOGGER.error("Shop conversion failed -- refusing to load shops this boot. Fix the reported error and restart.");
			shops = new LinkedList<>();
			return;
		}

		shopStore = new SnbtShopStore(itemCodec, ConfigManager.shopsDir());
		shops = new LinkedList<>(shopStore.readAll());
		shops.forEach(Shop::validate);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("GUI Shop loaded!");

		if (!LegacyConfigMigrator.migrateIfNeeded()) {
			throw new RuntimeException("Legacy config migration failed -- see log for details");
		}

		loadConfig();
		Register.registerCommands();
		GuiShopEconomyProvider.init();
	}

	public static void onServerShutdown() {
		try {
			ConfigManager.saveConfig();
			LOGGER.info("Config saved to file");
		} catch (IOException e) {
			LOGGER.error("Could not save config on shutdown", e);
		}
	}
}
