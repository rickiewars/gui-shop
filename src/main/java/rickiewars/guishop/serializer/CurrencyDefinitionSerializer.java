package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.config.GuiShopConfig.CurrencyDefinition;

import java.lang.reflect.Type;

public class CurrencyDefinitionSerializer implements JsonSerializer<CurrencyDefinition>, JsonDeserializer<CurrencyDefinition> {
    @Override
    public CurrencyDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject currency = jsonElement.getAsJsonObject();

        String iconString = currency.has("icon")
                ? currency.get("icon").getAsString()
                : GuiShopEconomyCurrency.DEFAULT_ICON_ID.toString();

        Identifier icon = Identifier.parse(iconString);
        if (!BuiltInRegistries.ITEM.containsKey(icon)) {
            GUIShop.LOGGER.warn("Invalid item id for currency icon: " + iconString);
            icon = GuiShopEconomyCurrency.DEFAULT_ICON_ID;
        }

        return new CurrencyDefinition(
                currency.get("name").getAsString(),
                currency.get("prefix").getAsString(),
                currency.get("suffix").getAsString(),
                currency.has("decimalPlaces") ? currency.get("decimalPlaces").getAsInt() : 2,
                icon
        );
    }

    @Override
    public JsonElement serialize(CurrencyDefinition currency, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("name", currency.name);
        result.addProperty("prefix", currency.prefix);
        result.addProperty("suffix", currency.suffix);
        result.addProperty("decimalPlaces", currency.decimalPlaces);
        result.addProperty("icon", currency.icon.toString());

        return result;
    }
}
