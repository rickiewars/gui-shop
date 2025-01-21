package rickiewars.guishop.util;


import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;
import rickiewars.guishop.shop.Shop;

import java.util.List;
import java.util.Optional;

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

    public static String pretty(long balance) {
        return String.format("$%d", balance);
    }

    public static Item getItem(String id) {
        return Registries.ITEM.get(Identifier.of(id));
    }
    public static Optional<Item> getOptionalItem(String id) {
        return Registries.ITEM.getOrEmpty(Identifier.of(id));
    }
    public static Item getItem(String id, Item defaultItem) {
        return getOptionalItem(id).orElse(defaultItem);
    }
    public static String getItemId(Item item) {
        return Registries.ITEM.getId(item).toString();
    }

}
