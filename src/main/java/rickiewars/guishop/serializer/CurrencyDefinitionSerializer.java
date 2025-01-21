package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config.CurrencyDefinition;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;
import rickiewars.guishop.util.CommonMethods;

import java.lang.reflect.Type;

public class CurrencyDefinitionSerializer implements JsonSerializer<CurrencyDefinition>, JsonDeserializer<CurrencyDefinition> {
    private static final String DEFAULT_ICON_ID = CommonMethods.getItemId(GuiShopEconomyCurrency.DEFAULT_ICON);

    @Override
    public CurrencyDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject currency = jsonElement.getAsJsonObject();

        String iconString = currency.has("icon")
                ? currency.get("icon").getAsString()
                : DEFAULT_ICON_ID;

        return new CurrencyDefinition(
                currency.get("name").getAsString(),
                currency.get("prefix").getAsString(),
                currency.get("suffix").getAsString(),
                new ItemStack(CommonMethods.getOptionalItem(iconString).orElseGet(() -> {
                    GUIShop.LOGGER.warn("Invalid item id for account icon: " + iconString);
                    return GuiShopEconomyCurrency.DEFAULT_ICON;
                }))
        );
    }

    @Override
    public JsonElement serialize(CurrencyDefinition currency, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("name", currency.name);
        result.addProperty("prefix", currency.prefix);
        result.addProperty("suffix", currency.suffix);
        result.addProperty("icon", CommonMethods.getItemId(currency.icon.getItem()));

        return result;
    }
}
