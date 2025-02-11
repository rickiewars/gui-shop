package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.config.EconomyConfig.AccountDefinition;
import rickiewars.guishop.config.EconomyConfig.CurrencyDefinition;
import rickiewars.guishop.config.EconomyConfig.EconomyProviderDefinition;

import java.lang.reflect.Type;

public class EconomyProviderDefinitionSerializer implements JsonSerializer<EconomyProviderDefinition>, JsonDeserializer<EconomyProviderDefinition> {
    private static final String JSON_PATH = "economy";
    @Override
    public EconomyProviderDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject economyDefinitionObject = jsonElement.getAsJsonObject();
        EconomyProviderDefinition economyDefinition = new EconomyProviderDefinition();

        if (economyDefinitionObject.has("currencies")) {
            JsonObject currencies = economyDefinitionObject.get("currencies").getAsJsonObject();
            currencies.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                try {
                    CurrencyDefinition currency = jsonDeserializationContext.deserialize(value, CurrencyDefinition.class);
                    economyDefinition.currencies.put(key, currency);
                } catch (IllegalStateException e) {
                    throw new JsonParseException(
                        "Invalid currency definition for " + String.join(JSON_PATH, "currencies", key) + ": "
                            + value + " is not an object"
                    );
                }
            });
        }

        if (economyDefinitionObject.has("accounts")) {
            JsonObject accounts = economyDefinitionObject.get("accounts").getAsJsonObject();
            accounts.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                try {
                    AccountDefinition account = jsonDeserializationContext.deserialize(value, AccountDefinition.class);
                    economyDefinition.accounts.put(key, account);
                } catch (IllegalStateException e) {
                    throw new JsonParseException(
                        "Invalid currency definition for " + String.join(JSON_PATH, "accounts", key) + ": "
                            + value + " is not an object"
                    );
                }
            });
        }

        return economyDefinition;
    }

    @Override
    public JsonElement serialize(EconomyProviderDefinition economy, Type type, JsonSerializationContext jsonSerializationContext) {
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
