package rickiewars.guishop.serializer;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.config.GuiShopConfig.CurrencyDefinition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyDefinitionSerializerTest extends MinecraftTest {

    // deserialize()

    @Test
    void malformedIconFallsBackToDefaultInsteadOfThrowing() {
        String json = "{\"name\":\"Coins\",\"prefix\":\"\",\"suffix\":\"\",\"icon\":\"Not An Id!\"}";

        CurrencyDefinition def = assertDoesNotThrow(
            () -> ConfigManager.GSON.fromJson(json, CurrencyDefinition.class),
            "a malformed icon must fall back to the default, not crash config loading"
        );

        assertEquals(GuiShopEconomyCurrency.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void unknownButValidSyntaxIconFallsBackToDefault() {
        String json = "{\"name\":\"Coins\",\"prefix\":\"\",\"suffix\":\"\",\"icon\":\"minecraft:not_a_real_item\"}";

        CurrencyDefinition def = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals(GuiShopEconomyCurrency.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void validIconIsUsedAsIs() {
        String json = "{\"name\":\"Coins\",\"prefix\":\"\",\"suffix\":\"\",\"icon\":\"minecraft:diamond\"}";

        CurrencyDefinition def = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals(ResourceId.ofVanilla("diamond"), def.icon);
    }

    @Test
    void missingIconFieldUsesDefaultIcon() {
        String json = "{\"name\":\"Coins\",\"prefix\":\"\",\"suffix\":\"\"}";

        CurrencyDefinition def = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals(GuiShopEconomyCurrency.DEFAULT_ICON_ID, def.icon);
    }

    @Test
    void deserializeReadsNamePrefixAndSuffix() {
        String json = "{\"name\":\"Coins\",\"prefix\":\"\",\"suffix\":\" coins\",\"decimalPlaces\":0,\"icon\":\"minecraft:diamond\"}";

        CurrencyDefinition def = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals("Coins", def.name);
        assertEquals("", def.prefix);
        assertEquals(" coins", def.suffix);
        assertEquals(0, def.decimalPlaces);
    }

    @Test
    void missingDecimalPlacesDefaultsToTwo() {
        String json = "{\"name\":\"Credits\",\"prefix\":\"$\",\"suffix\":\"\",\"icon\":\"minecraft:diamond\"}";

        CurrencyDefinition def = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals(2, def.decimalPlaces);
    }

    // serialize()

    @Test
    void serializeWritesEveryFieldAndFullIcon() {
        CurrencyDefinition def = new CurrencyDefinition("Credits", "$", "", 2, ResourceId.ofVanilla("diamond"));

        JsonObject json = ConfigManager.GSON.toJsonTree(def, CurrencyDefinition.class).getAsJsonObject();

        assertEquals("Credits", json.get("name").getAsString());
        assertEquals("$", json.get("prefix").getAsString());
        assertEquals("", json.get("suffix").getAsString());
        assertEquals(2, json.get("decimalPlaces").getAsInt());
        assertEquals("minecraft:diamond", json.get("icon").getAsString());
    }

    @Test
    void serializeThenDeserializeRoundTrips() {
        CurrencyDefinition original = new CurrencyDefinition("Credits", "$", "", 2, ResourceId.ofVanilla("diamond"));

        String json = ConfigManager.GSON.toJson(original, CurrencyDefinition.class);
        CurrencyDefinition roundTripped = ConfigManager.GSON.fromJson(json, CurrencyDefinition.class);

        assertEquals(original.name, roundTripped.name);
        assertEquals(original.prefix, roundTripped.prefix);
        assertEquals(original.suffix, roundTripped.suffix);
        assertEquals(original.decimalPlaces, roundTripped.decimalPlaces);
        assertEquals(original.icon, roundTripped.icon);
    }
}
