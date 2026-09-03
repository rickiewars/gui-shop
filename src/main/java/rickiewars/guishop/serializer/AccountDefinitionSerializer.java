package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.config.GuiShopConfig.AccountDefinition;

import java.lang.reflect.Type;

public class AccountDefinitionSerializer implements JsonSerializer<AccountDefinition>, JsonDeserializer<AccountDefinition> {
    @Override
    public AccountDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject account = jsonElement.getAsJsonObject();

        String iconString = account.has("icon")
                ? account.get("icon").getAsString()
                : GuiShopEconomyAccount.DEFAULT_ICON_ID.toString();

        Identifier icon = Identifier.parse(iconString);
        if (!BuiltInRegistries.ITEM.containsKey(icon)) {
            GUIShop.LOGGER.warn("Invalid item id for account icon: " + iconString);
            icon = GuiShopEconomyAccount.DEFAULT_ICON_ID;
        }

        return new AccountDefinition(
                account.get("currency").getAsString(),
                account.get("name").getAsString(),
                icon
        );
    }

    @Override
    public JsonElement serialize(AccountDefinition account, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("name", account.name);
        result.addProperty("currency", account.currencyId.getPath());
        result.addProperty("icon", account.icon.toString());

        return result;
    }
}
