package rickiewars.guishop.serializer;

import com.google.gson.*;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
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
        Identifier defaultCurrencyId = null;
        if (jsonShop.has("defaultCurrency")) {
            String defaultCurrencyIdStr = jsonShop.get("defaultCurrency").getAsString();
            defaultCurrencyId = defaultCurrencyIdStr.contains(":")
                    ? Identifier.of(defaultCurrencyIdStr)
                    : Identifier.of(GUIShop.MODID, defaultCurrencyIdStr);
        }

        String shopName = jsonShop.get("shopName").getAsString();
        Identifier icon = jsonShop.has("icon")
            ? Identifier.of(jsonShop.get("icon").getAsString())
            : Identifier.ofVanilla("chest");

        for(int i = 0; i < jsonItems.size(); i++){
            ShopItem item = jsonDeserializationContext.deserialize(jsonItems.get(i), ShopItem.class);
            items[i] = item;
        }

        LinkedList<ShopItem> shopItems = new LinkedList<>();
        for (ShopItem item: items) {
            shopItems.addLast(item);
        }

        return new Shop(shopName, shopItems, defaultCurrencyId, icon);
    }

    @Override
    public JsonElement serialize(Shop shop, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement items = jsonSerializationContext.serialize(shop.getItems().toArray());
        JsonElement shopName = new JsonPrimitive(shop.getName());
        JsonElement iconId = new JsonPrimitive(shop.iconId().toString());

        JsonObject jsonShop = new JsonObject();
        jsonShop.add("shopName", shopName);
        jsonShop.add("icon", iconId);
        if (shop.hasDefaultCurrency()) {
            JsonElement defaultCurrencyId = new JsonPrimitive(shop.getDefaultCurrencyId().toString());
            jsonShop.add("defaultCurrency", defaultCurrencyId);
        }
        jsonShop.add("items", items);

        return jsonShop;
    }
}
