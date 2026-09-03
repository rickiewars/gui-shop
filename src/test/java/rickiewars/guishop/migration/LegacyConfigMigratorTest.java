package rickiewars.guishop.migration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers config/guishop.json + config/guishopeconomy.json -> config/gui-shop/config.json.
 * The happy path is driven by the maintainer's real pre-migration backups, checked in under
 * src/test/resources/legacy/, so the assertions here are against real user data rather than
 * a hand-written idealisation of it.
 */
public class LegacyConfigMigratorTest extends MigrationTestBase {

    private GuiShopConfig readMigratedConfig() throws IOException {
        return ConfigManager.GSON.fromJson(Files.readString(configFile, StandardCharsets.UTF_8), GuiShopConfig.class);
    }

    private JsonObject readMigratedJson() throws IOException {
        return JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    void migratesTheRealWorldConfigPair() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());
        assertTrue(Files.exists(configFile), "config/gui-shop/config.json was not written");

        GuiShopConfig config = readMigratedConfig();

        assertFalse(config.economyDisabled);

        assertNotNull(config.database);
        assertEquals(GuiShopConfig.DatabaseConfig.DatabaseType.SQLITE, config.database.type);
        assertEquals("./world/guishop.sqlite", config.database.fileLocation);

        assertNotNull(config.command);
        assertFalse(config.command.disabled);
        assertEquals("balance", config.command.alias);

        assertNotNull(config.economy);
        assertEquals(2, config.economy.currencies.size());
        GuiShopConfig.CurrencyDefinition coins = config.economy.currencies.get("coins");
        assertEquals("Coins", coins.name);
        assertEquals("", coins.prefix);
        assertEquals(" coins", coins.suffix);
        assertEquals(0, coins.decimalPlaces);
        assertEquals(Identifier.parse("minecraft:diamond"), coins.icon);

        GuiShopConfig.CurrencyDefinition credit = config.economy.currencies.get("credit");
        assertEquals("Credits", credit.name);
        assertEquals("$", credit.prefix);
        assertEquals("", credit.suffix);
        assertEquals(2, credit.decimalPlaces);

        assertEquals(2, config.economy.accounts.size());
        assertEquals("Pouch", config.economy.accounts.get("pouch").name);
        assertEquals(Identifier.parse("guishop:coins"), config.economy.accounts.get("pouch").currencyId);
        assertEquals("Account", config.economy.accounts.get("account").name);
        assertEquals(Identifier.parse("guishop:credit"), config.economy.accounts.get("account").currencyId);

        assertEquals(
            List.of("account"),
            config.economyProviders.get(Identifier.parse("guishop:credit")),
            "dropping economyProviders would wipe the account-to-currency wiring"
        );
        assertEquals(List.of("pouch"), config.economyProviders.get(Identifier.parse("guishop:coins")));
    }

    @Test
    void migratedFileMatchesTheMaintainersShippedResult() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        JsonObject produced = readMigratedJson();
        JsonObject shipped = JsonParser.parseString(readFixture("expected-migrated-config.json")).getAsJsonObject();

        for (String key : List.of("economyDisabled", "database", "economy", "command", "economyProviders")) {
            assertEquals(shipped.get(key), produced.get(key), "block '" + key + "' differs from the real migrated config");
        }
    }

    @Test
    void backsUpBothLegacyFilesBeforeWriting() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");
        String shopsBefore = Files.readString(legacyShopFile, StandardCharsets.UTF_8);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        assertTrue(Files.exists(backupOf(legacyShopFile)));
        assertTrue(Files.exists(backupOf(legacyEconomyFile)));
        assertEquals(shopsBefore, Files.readString(backupOf(legacyShopFile), StandardCharsets.UTF_8));
        assertTrue(Files.exists(legacyShopFile), "the converter still needs guishop.json after this step");
    }

    @Test
    void leavesNoTempFileBehind() throws IOException {
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        assertFalse(Files.exists(guiShopDir.resolve("config.json.tmp")));
    }

    @Test
    void isIdempotentOnceTheNewConfigExists() throws IOException {
        installLegacyShops("guishop.json");
        installLegacyEconomy("guishopeconomy.json");
        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        String first = Files.readString(configFile, StandardCharsets.UTF_8);
        String handEdited = first + System.lineSeparator();
        Files.writeString(configFile, handEdited, StandardCharsets.UTF_8);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded(), "a second run must be a no-op, not a rewrite");
        assertEquals(handEdited, Files.readString(configFile, StandardCharsets.UTF_8));
    }

    @Test
    void doesNothingOnAFreshInstall() {
        assertTrue(LegacyConfigMigrator.migrateIfNeeded());
        assertFalse(Files.exists(configFile), "a fresh install must be left to ConfigManager.loadConfig()");
    }

    @Test
    void recoversProvidersWhenOnlyTheShopFileExists() throws IOException {
        installLegacyShops("guishop.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertEquals(2, config.economyProviders.size());
        assertNull(config.economy, "no legacy economy file means loadConfig() supplies the defaults");
        assertNull(config.database);
    }

    @Test
    void migratesEconomyWhenOnlyTheEconomyFileExists() throws IOException {
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertNotNull(config.economy);
        assertEquals(2, config.economy.currencies.size());
        assertTrue(config.economyProviders.isEmpty(), "providers cannot be recovered without guishop.json");
    }

    @Test
    void namespacesBareProviderKeysToGuishop() throws IOException {
        writeLegacyShops("{\"economyProviders\":{\"credit\":[\"account\"]},\"shops\":[]}");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        assertEquals(List.of("account"), readMigratedConfig().economyProviders.get(Identifier.parse("guishop:credit")));
    }

    @Test
    void appliesDefaultsForOmittedCurrencyAndAccountFields() throws IOException {
        writeLegacyEconomy("""
            {
              "economy": {
                "currencies": { "credit": { "name": "Credits", "prefix": "$", "suffix": "" } },
                "accounts": { "account": { "name": "Account", "currency": "credit" } }
              }
            }
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertEquals(2, config.economy.currencies.get("credit").decimalPlaces, "decimalPlaces defaults to 2");
        assertEquals(GuiShopEconomyCurrency.DEFAULT_ICON_ID, config.economy.currencies.get("credit").icon);
        assertEquals(GuiShopEconomyAccount.DEFAULT_ICON_ID, config.economy.accounts.get("account").icon);
    }

    @Test
    void fallsBackToTheDefaultIconWhenTheLegacyIconIsMalformed() throws IOException {
        writeLegacyEconomy("""
            {
              "economy": {
                "currencies": { "credit": { "name": "Credits", "prefix": "$", "suffix": "", "icon": "Not An Id!" } },
                "accounts": {}
              }
            }
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded(), "a bad icon must not abort the migration");
        assertEquals(GuiShopEconomyCurrency.DEFAULT_ICON_ID, readMigratedConfig().economy.currencies.get("credit").icon);
    }

    @Test
    void missingDatabaseBlockLeavesTheDatabaseUnset() throws IOException {
        writeLegacyEconomy("{\"disabled\":true}");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertTrue(config.economyDisabled);
        assertNull(config.database);
    }

    @Test
    void databaseBlockWithoutFileLocationUsesTheDefault() throws IOException {
        writeLegacyEconomy("{\"database\":{\"type\":\"sqlite\"}}");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        assertEquals(GuiShopConfig.DatabaseConfig.DEFAULT_FILE_LOCATION, readMigratedConfig().database.fileLocation);
    }

    // --- hand-edited legacy files --------------------------------------------------------------

    @Test
    void invalidProviderKeyIsSkippedAndTheRestMigrates() throws IOException {
        writeLegacyShops("""
            {"economyProviders":{"MyMod:Coins":["pouch"],"guishop:credit":["account"]},"shops":[]}
            """);
        installLegacyEconomy("guishopeconomy.json");

        assertTrue(LegacyConfigMigrator.migrateIfNeeded(), "one unparseable provider key must not brick the boot");

        GuiShopConfig config = readMigratedConfig();
        assertEquals(List.of("account"), config.economyProviders.get(Identifier.parse("guishop:credit")));
        assertEquals(1, config.economyProviders.size(), "the invalid key is dropped, not kept");
        assertEquals(2, config.economy.currencies.size(), "the economy file is unaffected by a bad provider key");
    }

    @Test
    void currencyMissingItsPrefixKeepsMigratingWithAnEmptyPrefix() throws IOException {
        writeLegacyEconomy("""
            {
              "economy": {
                "currencies": {
                  "coins": { "name": "Coins", "suffix": " coins" },
                  "credit": { "name": "Credits", "prefix": "$", "suffix": "" }
                },
                "accounts": {}
              }
            }
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertEquals("", config.economy.currencies.get("coins").prefix, "a missing prefix is cosmetic, default it");
        assertEquals(" coins", config.economy.currencies.get("coins").suffix);
        assertEquals("$", config.economy.currencies.get("credit").prefix, "the healthy currency must survive");
    }

    @Test
    void currencyMissingItsNameFallsBackToTheKey() throws IOException {
        writeLegacyEconomy("""
            {
              "economy": {
                "currencies": { "coins": { "prefix": "", "suffix": " coins" } },
                "accounts": {}
              }
            }
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        assertEquals("coins", readMigratedConfig().economy.currencies.get("coins").name);
    }

    @Test
    void accountMissingItsCurrencyIsSkippedAndTheRestMigrates() throws IOException {
        writeLegacyEconomy("""
            {
              "economy": {
                "currencies": { "credit": { "name": "Credits", "prefix": "$", "suffix": "" } },
                "accounts": {
                  "broken": { "name": "Broken" },
                  "account": { "name": "Account", "currency": "credit" }
                }
              }
            }
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded(), "an account with no currency is unusable, but only that account");

        GuiShopConfig config = readMigratedConfig();
        assertFalse(config.economy.accounts.containsKey("broken"), "an account with no currency cannot be resolved, drop it");
        assertEquals(Identifier.parse("guishop:credit"), config.economy.accounts.get("account").currencyId);
        assertEquals(1, config.economy.currencies.size());
    }

    @Test
    void scalarProviderValueIsSkippedAndTheRestMigrates() throws IOException {
        writeLegacyShops("""
            {"economyProviders":{"guishop:credit":"account","guishop:coins":["pouch"]},"shops":[]}
            """);

        assertTrue(LegacyConfigMigrator.migrateIfNeeded());

        GuiShopConfig config = readMigratedConfig();
        assertEquals(List.of("pouch"), config.economyProviders.get(Identifier.parse("guishop:coins")));
        assertFalse(config.economyProviders.containsKey(Identifier.parse("guishop:credit")));
    }

    /** A truncated or corrupt legacy file must fail closed, never half-write config.json. */
    @Test
    void corruptLegacyFileFailsClosed() throws IOException {
        writeLegacyEconomy("{\"economy\": {\"currencies\": ");

        assertFalse(LegacyConfigMigrator.migrateIfNeeded());
        assertFalse(Files.exists(configFile));
        assertTrue(Files.exists(backupOf(legacyEconomyFile)), "the backup is still taken before parsing");
    }
}
