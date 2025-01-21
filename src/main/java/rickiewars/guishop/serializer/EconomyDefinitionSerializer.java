package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.config.Config.AccountDefinition;
import rickiewars.guishop.config.Config.CurrencyDefinition;
import rickiewars.guishop.config.Config.EconomyDefinition;

import java.lang.reflect.Type;

public class EconomyDefinitionSerializer implements JsonSerializer<EconomyDefinition>, JsonDeserializer<EconomyDefinition> {
    private static final String JSON_PATH = "economy";
    @Override
    public EconomyDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject economy = jsonElement.getAsJsonObject();
        EconomyDefinition economyConfig = new EconomyDefinition();

        if (economy.has("currencies")) {
            JsonObject currencies = economy.get("currencies").getAsJsonObject();
            currencies.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                try {
                    CurrencyDefinition currency = jsonDeserializationContext.deserialize(value, CurrencyDefinition.class);
                    economyConfig.currencies.put(key, currency);
                } catch (IllegalStateException e) {
                    throw new JsonParseException(
                        "Invalid currency definition for " + String.join(JSON_PATH, "currencies", key) + ": "
                            + value + " is not an object"
                    );
                }
            });
        }

        if (economy.has("accounts")) {
            JsonObject accounts = economy.get("accounts").getAsJsonObject();
            accounts.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                try {
                    AccountDefinition account = jsonDeserializationContext.deserialize(value, AccountDefinition.class);
                    economyConfig.accounts.put(key, account);
                } catch (IllegalStateException e) {
                    throw new JsonParseException(
                        "Invalid currency definition for " + String.join(JSON_PATH, "accounts", key) + ": "
                            + value + " is not an object"
                    );
                }
            });
        }

        return economyConfig;
    }

    @Override
    public JsonElement serialize(EconomyDefinition economy, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();

        if (economy.currencies != null && !economy.currencies.isEmpty()) {
            JsonObject currencies = new JsonObject();
            economy.currencies.forEach((key, value) -> {
                JsonObject currency = jsonSerializationContext.serialize(value).getAsJsonObject();
                currencies.add(key, currency);
            });
            result.add("currencies", currencies);
        }

        if (economy.accounts != null && !economy.accounts.isEmpty()) {
            JsonObject accounts = new JsonObject();
            economy.accounts.forEach((key, value) -> {
                JsonObject account = jsonSerializationContext.serialize(value).getAsJsonObject();
                accounts.add(key, account);
            });
            result.add("accounts", accounts);
        }

        return result;
    }
}
