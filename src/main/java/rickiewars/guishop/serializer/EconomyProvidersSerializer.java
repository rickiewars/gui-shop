package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.GuiShopConfig;

import java.lang.reflect.Type;
import java.util.List;

public class EconomyProvidersSerializer implements JsonSerializer<GuiShopConfig.EconomyProviders>, JsonDeserializer<GuiShopConfig.EconomyProviders> {
    @Override
    public GuiShopConfig.EconomyProviders deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject economy = jsonElement.getAsJsonObject();
        GuiShopConfig.EconomyProviders economyProviders = new GuiShopConfig.EconomyProviders();

        economy.asMap().forEach((key, value) -> {
            ResourceId id = key.contains(":")
                ? ResourceId.parse(key)
                : ResourceId.of(GUIShop.MODID, key);
            List<String> accounts = value.getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .toList();
            economyProviders.put(id, accounts);
        });

        return economyProviders;
    }

    @Override
    public JsonElement serialize(GuiShopConfig.EconomyProviders providers, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();

        providers.forEach((key, value) -> {
            JsonArray accounts = new JsonArray();
            value.forEach(accounts::add);
            result.add(key.toString(), accounts);
        });

        return result;
    }
}
