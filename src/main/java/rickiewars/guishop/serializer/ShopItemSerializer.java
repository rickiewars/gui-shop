package rickiewars.guishop.serializer;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.component.ComponentChanges;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.shop.ShopItem;

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

        long buyItemPrice = jsonShop.get("buyPrice").getAsLong();
        long sellItemPrice = jsonShop.get("sellPrice").getAsLong();

        Identifier currencyId = null;
        if (jsonShop.has("currency")) {
            String currencyIdStr = jsonShop.get("currency").getAsString();
            currencyId = currencyIdStr.contains(":")
                    ? Identifier.of(currencyIdStr)
                    : Identifier.of(GuiShopEconomyProvider.ID, currencyIdStr);
        }

        // TODO: Put under test. If DynamicRegistryManager does not work, try:
        //      net.minecraft.registry.BuiltinRegistries.createWrapperLookup().getOps(JsonOps.INSTANCE)

        ComponentChanges componentChanges = ComponentChanges.CODEC.parse(
            DynamicRegistryManager.of(Registries.REGISTRIES).getOps(JsonOps.INSTANCE), jsonShop.get("components")
        ).resultOrPartial().orElse(null);

        return new ShopItem(itemName, itemId, buyItemPrice, sellItemPrice, currencyId, description, componentChanges);
    }

    @Override
    public JsonElement serialize(ShopItem shopItem, Type type, JsonSerializationContext jsonSerializationContext) {
        String itemName = shopItem.itemName();
        String itemId = shopItem.itemId();
        long buyItemPrice = shopItem.buyItemPrice();
        long sellItemPrice = shopItem.sellItemPrice();
        String[] description = shopItem.description();

        JsonElement jsonComponentChanges;
        if (shopItem.hasComponentChanges()) {
            jsonComponentChanges = ComponentChanges.CODEC.encodeStart(
                DynamicRegistryManager.of(Registries.REGISTRIES).getOps(JsonOps.INSTANCE), shopItem.componentChanges()
            ).resultOrPartial().orElseThrow();
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
        if (shopItem.hasCurrency()) {
            Identifier currencyId = shopItem.currencyId();
            finalResult.add("currency", new JsonPrimitive(currencyId.toString()));
        }
        finalResult.add("components", jsonComponentChanges);

        return finalResult;
    }
}
