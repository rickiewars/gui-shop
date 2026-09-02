package rickiewars.guishop.migration;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig;
import rickiewars.guishop.serializer.SnbtShopStore;
import rickiewars.guishop.shop.Shop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the three migration steps in the exact order GUIShop#onServerStarted does, over the
 * maintainer's real pre-migration files, and then loads the result the way the mod would.
 * This is the closest thing to "boot the mod once on a 1.x server" that a unit test can be.
 */
public class MigrationEndToEndTest extends MigrationTestBase {

    /** Mirrors GUIShop#onServerStarted, minus the parts that need a live server. */
    private boolean runBootSequence() {
        boolean configMigrationOk = LegacyConfigMigrator.migrateIfNeeded();
        boolean shopConversionOk = LegacyShopConverter.convertIfNeeded(registries(), itemCodec());
        LegacyMigrationCleanup.cleanupIfComplete(configMigrationOk, shopConversionOk);
        return configMigrationOk && shopConversionOk;
    }

    private List<Shop> loadShops() {
        return new SnbtShopStore(itemCodec(), shopsDir).readAll().stream()
            .sorted(Comparator.comparing(Shop::getId))
            .toList();
    }

    @Test
    void migratesARealInstallInOneBoot() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(runBootSequence());

        // config
        GuiShopConfig config = ConfigManager.GSON.fromJson(
            Files.readString(configFile, StandardCharsets.UTF_8), GuiShopConfig.class
        );
        assertEquals("balance", config.command.alias);
        assertEquals(2, config.economy.currencies.size());
        assertEquals(List.of("account"), config.economyProviders.get(Identifier.of("guishop:credit")));

        // shops
        List<Shop> shops = loadShops();
        assertEquals(
            List.of("example_shop", "example_shop_2", "example_shop_3", "example_shop_4"),
            shops.stream().map(Shop::getId).toList()
        );
        assertEquals(104, shops.stream().mapToInt(s -> s.getItems().size()).sum());
        assertEquals(101, shops.get(3).getItems().size());
        assertEquals(
            Identifier.of("guishop:coins"),
            shops.get(3).getItems().get(2).explicitCurrencyId(),
            "the one item priced in coins must keep its currency"
        );

        // aftermath
        assertTrue(Files.exists(doneMarker()));
        assertFalse(Files.exists(legacyShopFile), "the legacy files are cleaned up once both steps succeed");
        assertFalse(Files.exists(legacyEconomyFile));
        assertTrue(Files.exists(backupOf(legacyShopFile)));
        assertTrue(Files.exists(backupOf(legacyEconomyFile)));
        assertEquals(readFixture("guishop.json"), Files.readString(backupOf(legacyShopFile), StandardCharsets.UTF_8));
    }

    @Test
    void theSecondBootChangesNothing() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");
        assertTrue(runBootSequence());

        String configAfterFirstBoot = Files.readString(configFile, StandardCharsets.UTF_8);
        List<String> shopFilesAfterFirstBoot = shopFiles();

        assertTrue(runBootSequence(), "every boot after the migration must be a clean no-op");

        assertEquals(configAfterFirstBoot, Files.readString(configFile, StandardCharsets.UTF_8));
        assertEquals(shopFilesAfterFirstBoot, shopFiles());
        assertEquals(104, loadShops().stream().mapToInt(s -> s.getItems().size()).sum());
    }

    @Test
    void aFreshInstallIsUntouched() throws IOException {
        assertTrue(runBootSequence());

        assertFalse(Files.exists(configFile), "a fresh install is ConfigManager.loadConfig()'s job");
        assertFalse(Files.exists(shopsDir));
    }

    /**
     * A user whose shops fail to convert must still be able to retry: the legacy files stay on disk,
     * the backups exist, and nothing half-converted is published.
     */
    @Test
    void aFailedShopConversionLeavesTheInstallRecoverable() throws IOException {
        writeLegacyShops("""
            {"economyProviders":{"guishop:credit":["account"]},
             "shops":[{"icon":"minecraft:chest","items":[]}]}
            """);
        installLegacyEconomy("guishopeconomy.json");

        assertFalse(runBootSequence());

        assertTrue(Files.exists(configFile), "the config half of the migration still succeeded");
        assertTrue(Files.exists(legacyShopFile), "cleanup must not run after a failed conversion");
        assertTrue(Files.exists(legacyEconomyFile));
        assertFalse(Files.exists(doneMarker()));
        assertTrue(loadShops().isEmpty());
    }

    private List<String> shopFiles() throws IOException {
        try (var files = Files.list(shopsDir)) {
            return files.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }
}
