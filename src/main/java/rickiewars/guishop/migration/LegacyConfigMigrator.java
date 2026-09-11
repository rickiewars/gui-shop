package rickiewars.guishop.migration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LegacyConfigMigrator {
    private LegacyConfigMigrator() {}

    public static boolean migrateIfNeeded() {
        Path newConfigFile = ConfigManager.guiShopConfigFile();
        if (Files.exists(newConfigFile)) return true;

        Path legacyShopFile = ConfigManager.configRoot().resolve("guishop.json");
        Path legacyEconomyFile = ConfigManager.configRoot().resolve("guishopeconomy.json");
        if (!Files.exists(legacyShopFile) && !Files.exists(legacyEconomyFile)) return true;

        GUIShop.LOGGER.info("Migrating legacy gui-shop config files into {}", newConfigFile);

        try {
            MigrationBackup.backup(legacyShopFile);
            MigrationBackup.backup(legacyEconomyFile);

            GuiShopConfig config = new GuiShopConfig();
            config.economyProviders = readEconomyProviders(legacyShopFile);
            readLegacyEconomyConfig(legacyEconomyFile, config);

            Files.createDirectories(ConfigManager.guiShopConfigDir());
            Path tmp = newConfigFile.resolveSibling(newConfigFile.getFileName() + ".tmp");
            Files.writeString(tmp, ConfigManager.GSON.toJson(config), StandardCharsets.UTF_8);
            Files.move(tmp, newConfigFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            GUIShop.LOGGER.info("Legacy config migrated successfully");
            return true;
        } catch (com.google.gson.JsonSyntaxException e) {
            GUIShop.LOGGER.error("Legacy config migration failed: one of the legacy files is not valid JSON ({})", e.getMessage());
            return false;
        } catch (Exception e) {
            GUIShop.LOGGER.error("Legacy config migration failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private static GuiShopConfig.EconomyProviders readEconomyProviders(Path legacyShopFile) throws IOException {
        GuiShopConfig.EconomyProviders providers = new GuiShopConfig.EconomyProviders();
        if (!Files.exists(legacyShopFile)) {
            GUIShop.LOGGER.warn("Legacy shop config file not found -- account/currency wiring could not be recovered");
            return providers;
        }

        JsonObject root = JsonParser.parseString(Files.readString(legacyShopFile, StandardCharsets.UTF_8)).getAsJsonObject();
        if (!root.has("economyProviders")) return providers;

        JsonObject economyProviders = root.getAsJsonObject("economyProviders");
        economyProviders.asMap().forEach((key, value) -> {
            try {
                ResourceId id = key.contains(":") ? ResourceId.parse(key) : ResourceId.of(GUIShop.MODID, key);
                List<String> accounts = value.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
                providers.put(id, accounts);
            } catch (Exception e) {
                GUIShop.LOGGER.warn("Skipping unusable legacy economy provider '{}': {}", key, e.getMessage());
            }
        });

        return providers;
    }

    private static void readLegacyEconomyConfig(Path legacyEconomyFile, GuiShopConfig config) throws IOException {
        if (!Files.exists(legacyEconomyFile)) return;

        JsonObject root = JsonParser.parseString(Files.readString(legacyEconomyFile, StandardCharsets.UTF_8)).getAsJsonObject();

        config.economyDisabled = root.has("disabled") && root.get("disabled").getAsBoolean();

        if (root.has("database")) {
            JsonObject database = root.getAsJsonObject("database");
            String fileLocation = database.has("fileLocation")
                ? database.get("fileLocation").getAsString()
                : GuiShopConfig.DatabaseConfig.DEFAULT_FILE_LOCATION;
            config.database = new GuiShopConfig.DatabaseConfig(GuiShopConfig.DatabaseConfig.DatabaseType.SQLITE, fileLocation);
        }

        if (root.has("command")) {
            JsonObject command = root.getAsJsonObject("command");
            boolean disabled = command.has("disabled") && command.get("disabled").getAsBoolean();
            String alias = command.has("alias") ? command.get("alias").getAsString() : "";
            config.command = new GuiShopConfig.CommandConfig(disabled, alias);
        }

        if (root.has("economy")) {
            JsonObject economy = root.getAsJsonObject("economy");
            Map<String, GuiShopConfig.CurrencyDefinition> currencies = new HashMap<>();
            Map<String, GuiShopConfig.AccountDefinition> accounts = new HashMap<>();

            if (economy.has("currencies")) {
                economy.getAsJsonObject("currencies").entrySet().forEach(entry -> {
                    try {
                        currencies.put(entry.getKey(), readCurrencyDefinition(entry.getKey(), entry.getValue().getAsJsonObject()));
                    } catch (Exception e) {
                        GUIShop.LOGGER.warn("Skipping unusable legacy currency '{}': {}", entry.getKey(), e.getMessage());
                    }
                });
            }
            if (economy.has("accounts")) {
                economy.getAsJsonObject("accounts").entrySet().forEach(entry -> {
                    try {
                        accounts.put(entry.getKey(), readAccountDefinition(entry.getKey(), entry.getValue().getAsJsonObject()));
                    } catch (Exception e) {
                        GUIShop.LOGGER.warn("Skipping unusable legacy account '{}': {}", entry.getKey(), e.getMessage());
                    }
                });
            }

            config.economy = new GuiShopConfig.EconomyProviderDefinition(currencies, accounts);
        }
    }

    /// Display fields are cosmetic and are defaulted when a hand-edited file omits them. Only a
    /// field the entry cannot function without throws, which skips that one entry.
    private static GuiShopConfig.CurrencyDefinition readCurrencyDefinition(String id, JsonObject currency) {
        ResourceId icon = readIcon(currency, rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency.DEFAULT_ICON_ID);
        return new GuiShopConfig.CurrencyDefinition(
            readString(currency, "name", id),
            readString(currency, "prefix", ""),
            readString(currency, "suffix", ""),
            currency.has("decimalPlaces") ? currency.get("decimalPlaces").getAsInt() : 2,
            icon
        );
    }

    private static GuiShopConfig.AccountDefinition readAccountDefinition(String id, JsonObject account) {
        if (!account.has("currency")) {
            throw new IllegalArgumentException("account has no currency, so it cannot be resolved");
        }

        ResourceId icon = readIcon(account, rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount.DEFAULT_ICON_ID);
        return new GuiShopConfig.AccountDefinition(
            account.get("currency").getAsString(),
            readString(account, "name", id),
            icon
        );
    }

    private static String readString(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private static ResourceId readIcon(JsonObject object, ResourceId fallback) {
        if (!object.has("icon")) return fallback;
        try {
            return ResourceId.parse(object.get("icon").getAsString());
        } catch (Exception e) {
            GUIShop.LOGGER.warn("Legacy icon value was not a plain item id string, using default: {}", e.getMessage());
            return fallback;
        }
    }
}
