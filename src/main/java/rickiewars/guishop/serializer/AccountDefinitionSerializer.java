package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config.AccountDefinition;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyAccount;
import rickiewars.guishop.util.CommonMethods;

import java.lang.reflect.Type;

public class AccountDefinitionSerializer implements JsonSerializer<AccountDefinition>, JsonDeserializer<AccountDefinition> {
    private static final String DEFAULT_ICON_ID = CommonMethods.getItemId(GuiShopEconomyAccount.DEFAULT_ICON);

    @Override
    public AccountDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject account = jsonElement.getAsJsonObject();

        String iconString = account.has("icon")
                ? account.get("icon").getAsString()
                : DEFAULT_ICON_ID;

        return new AccountDefinition(
                account.get("currency").getAsString(),
                account.get("name").getAsString(),
                new ItemStack(CommonMethods.getOptionalItem(iconString).orElseGet(() -> {
                    GUIShop.LOGGER.warn("Invalid item id for account icon: " + iconString);
                    return GuiShopEconomyAccount.DEFAULT_ICON;
                }))
        );

    }

    @Override
    public JsonElement serialize(AccountDefinition economy, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("name", economy.name);
        result.addProperty("currency", economy.currencyId);
        result.addProperty("icon", CommonMethods.getItemId(economy.icon.getItem()));

        return result;
    }
}
