package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.config.Config;
import rickiewars.guishop.config.Config.EconomyProviders;

import java.lang.reflect.Type;
import java.util.Map;

public class EconomyProvidersSerializer implements JsonSerializer<Config.EconomyProviders>, JsonDeserializer<EconomyProviders> {
    private static final String CONFIG_PATH = "economy";

    @Override
    public EconomyProviders deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject economy = jsonElement.getAsJsonObject();
        EconomyProviders economyConfig = new EconomyProviders();

        if (economy.has("currencies")) {
            deserializeMap(economy.get("currencies"), economyConfig.currencies, "Invalid currency definition for ", ".currencies.");
        }

        if (!economy.has("accounts")) {
            return economyConfig;
        }
        deserializeMap(economy.get("accounts"), economyConfig.accounts, "Invalid account definition for ", ".accounts.");

        return economyConfig;
    }

    private static void deserializeMap(JsonElement jsonElement, Map<String, String> economyConfig, String x, String x1) {
        JsonObject obj = jsonElement.getAsJsonObject();
        obj.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            try {
                String currencyId = value.getAsString();
                economyConfig.put(key, currencyId);
            } catch (IllegalStateException e) {
                throw new JsonParseException(
                        x + CONFIG_PATH + x1 + key + ": "
                                + value + " is not a string"
                );
            }
        });
    }

    @Override
    public JsonElement serialize(EconomyProviders economy, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();

        if (economy.currencies != null && !economy.currencies.isEmpty()) {
            JsonObject currencies = new JsonObject();
            economy.currencies.forEach((key, value) -> {
                currencies.add(key, new JsonPrimitive(value));
            });
            result.add("currencies", currencies);
        }

        if (economy.accounts != null && !economy.accounts.isEmpty()) {
            JsonObject accounts = new JsonObject();
            economy.accounts.forEach((key, value) -> {
                accounts.add(key, new JsonPrimitive(value));
            });
            result.add("accounts", accounts);
        }

        return result;
    }
}
