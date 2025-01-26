package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class ShopSerializer implements JsonSerializer<Shop>, JsonDeserializer<Shop> {
    @Override
    public Shop deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonShop = jsonElement.getAsJsonObject();
        JsonArray jsonItems = jsonShop.getAsJsonArray("items");
        ShopItem[] items = new ShopItem[jsonItems.size()];
        String defaultCurrencyId = null;
        if (jsonShop.has("defaultCurrency")) {
            defaultCurrencyId = jsonShop.get("defaultCurrency").getAsString();
        }

        String shopName = jsonShop.get("shopName").getAsString();
        for(int i = 0; i < jsonItems.size(); i++){
            ShopItem item = jsonDeserializationContext.deserialize(jsonItems.get(i), ShopItem.class);
            items[i] = item;
        }

        LinkedList<ShopItem> shopItems = new LinkedList<>();
        for (ShopItem item: items) {
            shopItems.addLast(item);
        }

        return new Shop(shopName, shopItems, defaultCurrencyId);
    }

    @Override
    public JsonElement serialize(Shop shop, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement items = jsonSerializationContext.serialize(shop.getItems().toArray());
        JsonElement shopName = new JsonPrimitive(shop.getName());

        JsonObject jsonShop = new JsonObject();
        jsonShop.add("shopName", shopName);
        if (shop.hasDefaultCurrency()) {
            JsonElement defaultCurrencyId = new JsonPrimitive(shop.getDefaultCurrencyId());
            jsonShop.add("defaultCurrency", defaultCurrencyId);
        }
        jsonShop.add("items", items);

        return jsonShop;
    }
}
