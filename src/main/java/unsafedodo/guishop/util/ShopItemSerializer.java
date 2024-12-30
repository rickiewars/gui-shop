package unsafedodo.guishop.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import unsafedodo.guishop.shop.ShopItem;

import java.lang.reflect.Type;

public class ShopItemSerializer implements JsonSerializer<ShopItem>, JsonDeserializer<ShopItem> {
    @Override
    public ShopItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonShop = jsonElement.getAsJsonObject();

        String itemName = jsonShop.get("name").getAsString();
        String itemMaterial = jsonShop.get("material").getAsString();

        JsonArray jsonDescription = jsonShop.getAsJsonArray("description");
        String[] description = new String[jsonDescription.size()];
        for(int i = 0; i < jsonDescription.size(); i++){
            description[i] = jsonDescription.get(i).getAsString();
        }

        float buyItemPrice = jsonShop.get("buyPrice").getAsFloat();
        float sellItemPrice = jsonShop.get("sellPrice").getAsFloat();

        NbtCompound nbt;

        try {
            String nbtString = jsonShop.get("nbt").getAsString();
            nbt = StringNbtReader.parse(nbtString);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        return new ShopItem(itemName, itemMaterial, buyItemPrice, sellItemPrice, description, nbt);
    }

    @Override
    public JsonElement serialize(ShopItem shopItem, Type type, JsonSerializationContext jsonSerializationContext) {
        String itemName = shopItem.getItemName();
        String itemMaterial = shopItem.getItemMaterial();
        float buyItemPrice = shopItem.getBuyItemPrice();
        float sellItemPrice = shopItem.getSellItemPrice();
        String[] description = shopItem.getDescription();
        NbtCompound nbt = shopItem.getNbt();

        JsonObject finalResult = new JsonObject();
        finalResult.add("name", new JsonPrimitive(itemName));
        finalResult.add("material", new JsonPrimitive(itemMaterial));

        JsonArray jsonDescription = new JsonArray(description.length);
        for (String s : description) {
            jsonDescription.add(s);
        }

        finalResult.add("description", jsonDescription);
        finalResult.add("buyPrice", new JsonPrimitive(buyItemPrice));
        finalResult.add("sellPrice", new JsonPrimitive(sellItemPrice));
        finalResult.add("nbt", new JsonPrimitive(nbt.toString()));

        return finalResult;
    }
}
