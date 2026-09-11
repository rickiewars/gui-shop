package rickiewars.guishop.serializer;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig.AccountDefinition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountDefinitionSerializerTest extends MinecraftTest {

    // deserialize()

    @Test
    void malformedIconFallsBackToDefaultInsteadOfThrowing() {
        String json = "{\"currency\":\"credit\",\"name\":\"Account\",\"icon\":\"Not An Id!\"}";

        AccountDefinition def = assertDoesNotThrow(
            () -> ConfigManager.GSON.fromJson(json, AccountDefinition.class),
            "a malformed icon must fall back to the default, not crash config loading"
        );

        assertEquals(GuiShopEconomyAccount.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void unknownButValidSyntaxIconFallsBackToDefault() {
        String json = "{\"currency\":\"credit\",\"name\":\"Account\",\"icon\":\"minecraft:not_a_real_item\"}";

        AccountDefinition def = ConfigManager.GSON.fromJson(json, AccountDefinition.class);

        assertEquals(GuiShopEconomyAccount.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void validIconIsUsedAsIs() {
        String json = "{\"currency\":\"credit\",\"name\":\"Account\",\"icon\":\"minecraft:diamond\"}";

        AccountDefinition def = ConfigManager.GSON.fromJson(json, AccountDefinition.class);

        assertEquals(ResourceId.ofVanilla("diamond"), def.icon);
    }

    @Test
    void missingIconFieldUsesDefaultIcon() {
        String json = "{\"currency\":\"credit\",\"name\":\"Account\"}";

        AccountDefinition def = ConfigManager.GSON.fromJson(json, AccountDefinition.class);

        assertEquals(GuiShopEconomyAccount.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void deserializeReadsNameAndNamespacesCurrencyToTheModId() {
        String json = "{\"currency\":\"coins\",\"name\":\"Pouch\",\"icon\":\"minecraft:diamond\"}";

        AccountDefinition def = ConfigManager.GSON.fromJson(json, AccountDefinition.class);

        assertEquals("Pouch", def.name);
        assertEquals(ResourceId.of("guishop", "coins"), def.currencyId);
    }

    // serialize()

    @Test
    void serializeWritesNameBarePathCurrencyAndFullIcon() {
        AccountDefinition def = new AccountDefinition("coins", "Pouch", ResourceId.ofVanilla("diamond"));

        JsonObject json = ConfigManager.GSON.toJsonTree(def, AccountDefinition.class).getAsJsonObject();

        assertEquals("Pouch", json.get("name").getAsString());
        assertEquals("coins", json.get("currency").getAsString());
        assertEquals("minecraft:diamond", json.get("icon").getAsString());
    }

    @Test
    void serializeThenDeserializeRoundTrips() {
        AccountDefinition original = new AccountDefinition("coins", "Pouch", ResourceId.ofVanilla("diamond"));

        String json = ConfigManager.GSON.toJson(original, AccountDefinition.class);
        AccountDefinition roundTripped = ConfigManager.GSON.fromJson(json, AccountDefinition.class);

        assertEquals(original.name, roundTripped.name);
        assertEquals(original.currencyId, roundTripped.currencyId);
        assertEquals(original.icon, roundTripped.icon);
    }
}
