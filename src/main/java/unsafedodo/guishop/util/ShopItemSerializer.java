package unsafedodo.guishop.util;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.component.ComponentChanges;
import unsafedodo.guishop.shop.ShopItem;

import java.lang.reflect.Type;

public class ShopItemSerializer implements JsonSerializer<ShopItem>, JsonDeserializer<ShopItem> {
    @Override
    public ShopItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonShop = jsonElement.getAsJsonObject();

        String itemName = jsonShop.get("name").getAsString();
        String itemId = jsonShop.get("itemId").getAsString();

        JsonArray jsonDescription = jsonShop.getAsJsonArray("description");
        String[] description = new String[jsonDescription.size()];
        for(int i = 0; i < jsonDescription.size(); i++){
            description[i] = jsonDescription.get(i).getAsString();
        }

        float buyItemPrice = jsonShop.get("buyPrice").getAsFloat();
        float sellItemPrice = jsonShop.get("sellPrice").getAsFloat();

        ComponentChanges componentChanges = ComponentChanges.CODEC.parse(
                JsonOps.INSTANCE, jsonShop.get("components")
        ).resultOrPartial().orElse(null);

        return new ShopItem(itemName, itemId, buyItemPrice, sellItemPrice, description, componentChanges);
    }

    @Override
    public JsonElement serialize(ShopItem shopItem, Type type, JsonSerializationContext jsonSerializationContext) {
        String itemName = shopItem.itemName();
        String itemId = shopItem.itemId();
        float buyItemPrice = shopItem.buyItemPrice();
        float sellItemPrice = shopItem.sellItemPrice();
        String[] description = shopItem.description();

        JsonElement jsonComponentChanges;
        if (shopItem.hasComponentChanges()) {
            jsonComponentChanges = ComponentChanges.CODEC.encodeStart(
                    JsonOps.INSTANCE, shopItem.componentChanges()
            ).resultOrPartial().orElse(null);
        } else {
            jsonComponentChanges = new JsonObject();
        }

        JsonObject finalResult = new JsonObject();
        finalResult.add("name", new JsonPrimitive(itemName));
        finalResult.add("itemId", new JsonPrimitive(itemId));

        JsonArray jsonDescription = new JsonArray(description.length);
        for (String s : description) {
            jsonDescription.add(s);
        }

        finalResult.add("description", jsonDescription);
        finalResult.add("buyPrice", new JsonPrimitive(buyItemPrice));
        finalResult.add("sellPrice", new JsonPrimitive(sellItemPrice));
        finalResult.add("components", jsonComponentChanges);

        return finalResult;
    }
}
