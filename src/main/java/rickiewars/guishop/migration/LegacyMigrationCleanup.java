package rickiewars.guishop.migration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.ConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyMigrationCleanup {
    private LegacyMigrationCleanup() {}

    public static void cleanupIfComplete(boolean configMigrationOk, boolean shopConversionOk) {
        if (!configMigrationOk || !shopConversionOk) return;
        if (!Files.exists(ConfigManager.guiShopConfigFile())) return;

        Path legacyShopFile = ConfigManager.configRoot().resolve("guishop.json");
        Path legacyEconomyFile = ConfigManager.configRoot().resolve("guishopeconomy.json");

        if (!shopsAlreadyHandled(legacyShopFile)) return;

        deleteIfExists(legacyShopFile);
        deleteIfExists(legacyEconomyFile);
    }

    private static boolean shopsAlreadyHandled(Path legacyShopFile) {
        if (Files.exists(ConfigManager.shopsDir().resolve(".conversion-complete"))) return true;
        if (!Files.exists(legacyShopFile)) return true;

        try {
            JsonObject root = JsonParser.parseString(Files.readString(legacyShopFile, StandardCharsets.UTF_8)).getAsJsonObject();
            return !root.has("shops") || root.getAsJsonArray("shops").isEmpty();
        } catch (Exception e) {
            // Malformed JSON throws unchecked. Treat the shops as unhandled so nothing is deleted.
            GUIShop.LOGGER.warn("Could not read legacy shop config {}, keeping the legacy files: {}", legacyShopFile, e.getMessage());
            return false;
        }
    }

    private static void deleteIfExists(Path file) {
        try {
            if (Files.deleteIfExists(file)) {
                GUIShop.LOGGER.info("Deleted legacy config file {} (backup retained as .pre-migration-backup)", file);
            }
        } catch (IOException e) {
            GUIShop.LOGGER.warn("Could not delete legacy config file {}: {}", file, e.getMessage());
        }
    }
}
