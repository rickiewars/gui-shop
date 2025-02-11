package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;

import java.lang.reflect.Type;
import java.util.List;

public class EconomyProvidersSerializer implements JsonSerializer<Config.EconomyProviders>, JsonDeserializer<Config.EconomyProviders> {
    @Override
    public Config.EconomyProviders deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject economy = jsonElement.getAsJsonObject();
        Config.EconomyProviders economyProviders = new Config.EconomyProviders();

        economy.asMap().forEach((key, value) -> {
            Identifier id = key.contains(":")
                ? Identifier.of(key)
                : Identifier.of(GUIShop.MODID, key);
            List<String> accounts = value.getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .toList();
            economyProviders.put(id, accounts);
        });

        return economyProviders;
    }

    @Override
    public JsonElement serialize(Config.EconomyProviders providers, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();

        providers.forEach((key, value) -> {
            JsonArray accounts = new JsonArray();
            value.forEach(accounts::add);
            result.add(key.toString(), accounts);
        });

        return result;
    }
}
