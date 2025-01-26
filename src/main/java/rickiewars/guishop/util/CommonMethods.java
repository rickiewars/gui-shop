package rickiewars.guishop.util;


import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;
import rickiewars.guishop.shop.Shop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommonMethods {

    public static String arrayImplode(int[] array, String delimiter) {
        StringBuilder strBldr = new StringBuilder();

        for (int i = 0; i < array.length - 1; i++) {
            strBldr.append(array[i]);
            strBldr.append(delimiter);
        }
        strBldr.append(array[array.length - 1]);

        return strBldr.toString();
    }

    /**
     * Gets shop data by name.
     * @param name The name of the shop to look for
     * @return An object of class Shop from the list {@link Config#shops} with the same case-sensitive name as the one
     * passed by argument, or null if none is found.
     */
    public static Shop getShopByName(String name) {
        for(Shop shop: GUIShop.config.shops){
            if(shop.getName().equals(name)){
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getAllShops(){
        return GUIShop.config.shops;
    }

    public static Throwable findRootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    public static Item getItem(String id) {
        return Registries.ITEM.get(Identifier.of(id));
    }
    public static Optional<Item> getOptionalItem(String id) {
        return Registries.ITEM.getOptionalValue(Identifier.of(id));
    }
    public static Item getItem(String id, Item defaultItem) {
        return getOptionalItem(id).orElse(defaultItem);
    }
    public static String getItemId(Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    public static String translatePlayer(UUID uuid) {
        ServerPlayerEntity player = ServerHandler.getPlayerByUUID(uuid);
        return player != null ? player.getName().getString() : uuid.toString();
    }

    // Make sure the string length is at least the specified length
    // If it is less, pad it with the specified character on the left
    public static String padLeft(String str, int length, char padChar) {
        if (str.length() >= length) return str;
        return String.valueOf(padChar).repeat(length - str.length()) + str;
    }

    /**
     * Insert a character into a string at a specified index
     * @param str The string to insert the character into
     * @param index The index to insert the character at. If negative, it will be counted from the end of the string
     * @param insertChar The character to insert
     * @return The new string with the character inserted
     */
    public static String insert(String str, int index, char insertChar) {
        if (index < 0) index = str.length() + index;
        return str.substring(0, index) + insertChar + str.substring(index);
    }

}
