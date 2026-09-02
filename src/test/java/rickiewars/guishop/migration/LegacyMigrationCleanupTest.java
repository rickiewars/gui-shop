package rickiewars.guishop.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cleanup is the only step that destroys user data, so every guard in front of the two deletes is
 * pinned here. The .pre-migration-backup copies must always survive it.
 */
public class LegacyMigrationCleanupTest extends MigrationTestBase {

    private void seedMigratedState() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");
        MigrationBackup.backup(legacyShopFile);
        MigrationBackup.backup(legacyEconomyFile);
        Files.createDirectories(guiShopDir);
        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);
        markConversionComplete();
    }

    private void assertLegacyFilesSurvive() {
        assertTrue(Files.exists(legacyShopFile), "guishop.json was deleted when it should not have been");
        assertTrue(Files.exists(legacyEconomyFile), "guishopeconomy.json was deleted when it should not have been");
    }

    @Test
    void deletesBothLegacyFilesButKeepsTheBackups() throws IOException {
        seedMigratedState();

        LegacyMigrationCleanup.cleanupIfComplete(true, true);

        assertFalse(Files.exists(legacyShopFile));
        assertFalse(Files.exists(legacyEconomyFile));
        assertTrue(Files.exists(backupOf(legacyShopFile)), "the backup is the user's only way back");
        assertTrue(Files.exists(backupOf(legacyEconomyFile)));
        assertEquals(readFixture("guishop.json"), Files.readString(backupOf(legacyShopFile), StandardCharsets.UTF_8));
    }

    @Test
    void keepsEverythingWhenTheConfigMigrationFailed() throws IOException {
        seedMigratedState();

        LegacyMigrationCleanup.cleanupIfComplete(false, true);

        assertLegacyFilesSurvive();
    }

    @Test
    void keepsEverythingWhenTheShopConversionFailed() throws IOException {
        seedMigratedState();

        LegacyMigrationCleanup.cleanupIfComplete(true, false);

        assertLegacyFilesSurvive();
    }

    @Test
    void keepsEverythingWhenTheNewConfigIsMissing() throws IOException {
        seedMigratedState();
        Files.delete(configFile);

        LegacyMigrationCleanup.cleanupIfComplete(true, true);

        assertLegacyFilesSurvive();
    }

    @Test
    void keepsEverythingWhenShopsAreStillUnconverted() throws IOException {
        seedMigratedState();
        Files.delete(doneMarker()); // no done marker == shop conversion never actually finished

        LegacyMigrationCleanup.cleanupIfComplete(true, true);

        assertLegacyFilesSurvive();
    }

    @Test
    void cleansUpWithoutADoneMarkerWhenThereWereNoShopsToConvert() throws IOException {
        writeLegacyShops("{\"economyProviders\":{\"guishop:credit\":[\"account\"]},\"shops\":[]}");
        installLegacyEconomy("guishopeconomy.json");
        Files.createDirectories(guiShopDir);
        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);

        LegacyMigrationCleanup.cleanupIfComplete(true, true);

        assertFalse(Files.exists(legacyShopFile), "there was nothing to convert, so no done marker is expected either");
        assertFalse(Files.exists(legacyEconomyFile));
    }

    @Test
    void cleansUpTheEconomyFileWhenTheShopFileIsAlreadyGone() throws IOException {
        installLegacyEconomy("guishopeconomy.json");
        Files.createDirectories(guiShopDir);
        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);

        LegacyMigrationCleanup.cleanupIfComplete(true, true);

        assertFalse(Files.exists(legacyEconomyFile));
    }

    @Test
    void isIdempotent() throws IOException {
        seedMigratedState();

        LegacyMigrationCleanup.cleanupIfComplete(true, true);
        assertDoesNotThrow(() -> LegacyMigrationCleanup.cleanupIfComplete(true, true));

        assertTrue(Files.exists(backupOf(legacyShopFile)));
    }

    /**
     * Cleanup must fail safe: if the legacy file cannot be understood, treat the shops as not yet
     * handled and delete nothing. shopsAlreadyHandled currently catches only IOException, so the
     * unchecked JsonSyntaxException escapes cleanupIfComplete and out of GUIShop.onServerStarted --
     * and unlike the other two migration classes this one has no outer catch (Exception) either.
     */
    @Test
    void corruptLegacyShopFileMustNotEscape() throws IOException {
        writeLegacyShops("{\"shops\": [");
        installLegacyEconomy("guishopeconomy.json");
        Files.createDirectories(guiShopDir);
        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);

        assertDoesNotThrow(
            () -> LegacyMigrationCleanup.cleanupIfComplete(true, true),
            "a corrupt guishop.json must not escape cleanup and abort server start"
        );
        assertLegacyFilesSurvive();
    }

    /** A legacy file that is not even a JSON object must be treated as 'not handled', not deleted. */
    @Test
    void nonObjectLegacyShopFileMustNotEscape() throws IOException {
        writeLegacyShops("[]");
        installLegacyEconomy("guishopeconomy.json");
        Files.createDirectories(guiShopDir);
        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);

        assertDoesNotThrow(
            () -> LegacyMigrationCleanup.cleanupIfComplete(true, true),
            "a legacy file that is not a JSON object must be treated as unhandled, not thrown"
        );
        assertLegacyFilesSurvive();
    }
}
