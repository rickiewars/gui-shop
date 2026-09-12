package rickiewars.guishop.migration;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.config.ConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * Shared setup for the migration tests. ConfigManager resolves every path against the process
 * working directory with no override (ConfigManager#configRoot), so the four path accessors are
 * stubbed onto a throwaway directory instead. GSON stays real -- it is a static field, which
 * mockStatic does not touch -- so the migrator still writes through the production serializers.
 */
public abstract class MigrationTestBase extends MinecraftTest {

    protected static final Path FIXTURE_DIR = Path.of("src/test/resources/legacy");

    private static HolderLookup.Provider registries;

    protected Path configRoot;
    protected Path guiShopDir;
    protected Path configFile;
    protected Path shopsDir;
    protected Path legacyShopFile;
    protected Path legacyEconomyFile;

    private MockedStatic<ConfigManager> configManagerMock;

    @BeforeEach
    final void setupMigrationPaths() throws IOException {
        configRoot = Files.createTempDirectory("guishop-migration");
        guiShopDir = configRoot.resolve("gui-shop");
        configFile = guiShopDir.resolve("config.json");
        shopsDir = guiShopDir.resolve("shops");
        legacyShopFile = configRoot.resolve("guishop.json");
        legacyEconomyFile = configRoot.resolve("guishopeconomy.json");

        configManagerMock = Mockito.mockStatic(ConfigManager.class, Mockito.CALLS_REAL_METHODS);
        configManagerMock.when(ConfigManager::configRoot).thenReturn(configRoot);
        configManagerMock.when(ConfigManager::guiShopConfigDir).thenReturn(guiShopDir);
        configManagerMock.when(ConfigManager::guiShopConfigFile).thenReturn(configFile);
        configManagerMock.when(ConfigManager::shopsDir).thenReturn(shopsDir);
    }

    @AfterEach
    final void teardownMigrationPaths() throws IOException {
        if (configManagerMock != null) {
            configManagerMock.close();
            configManagerMock = null;
        }
        deleteRecursively(configRoot);
    }

    protected static HolderLookup.Provider registries() {
        if (registries == null) registries = VanillaRegistries.createLookup();
        return registries;
    }

    protected static MinecraftItemCodec itemCodec() {
        return new MinecraftItemCodec(registries());
    }

    /** Copies a fixture from src/test/resources/legacy into the temp config root as guishop.json. */
    protected void installLegacyShops(String fixtureName) throws IOException {
        Files.copy(FIXTURE_DIR.resolve(fixtureName), legacyShopFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Copies a fixture into the temp config root as guishopeconomy.json. */
    protected void installLegacyEconomy(String fixtureName) throws IOException {
        Files.copy(FIXTURE_DIR.resolve(fixtureName), legacyEconomyFile, StandardCopyOption.REPLACE_EXISTING);
    }

    protected void writeLegacyShops(String json) throws IOException {
        Files.writeString(legacyShopFile, json, StandardCharsets.UTF_8);
    }

    protected void writeLegacyEconomy(String json) throws IOException {
        Files.writeString(legacyEconomyFile, json, StandardCharsets.UTF_8);
    }

    protected Path backupOf(Path legacyFile) {
        return legacyFile.resolveSibling(legacyFile.getFileName() + ".pre-migration-backup");
    }

    protected Path doneMarker() {
        return shopsDir.resolve(".conversion-complete");
    }

    protected void markConversionComplete() throws IOException {
        Files.createDirectories(shopsDir);
        Files.writeString(doneMarker(), "", StandardCharsets.UTF_8);
    }

    protected String readFixture(String fixtureName) throws IOException {
        return Files.readString(FIXTURE_DIR.resolve(fixtureName), StandardCharsets.UTF_8);
    }

    protected static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {}
            });
        }
    }
}
