package rickiewars.guishop.util;


import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;

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
     * Gets shop data by display name.
     * @param name The display name of the shop to look for
     * @return An object of class Shop from {@link GUIShop#shops} with the same case-sensitive
     * display name as the one passed by argument, or null if none is found.
     */
    public static Shop getShopByName(String name) {
        for(Shop shop: GUIShop.shops){
            if(shop.getDisplayName().equals(name)){
                return shop;
            }
        }
        return null;
    }

    /// Turn a display name into a filesystem/id-safe slug: lowercase, non-alphanumerics become
    /// `_`, and collisions with `existingIds` get `_2`, `_3`, ... appended.
    public static String slugify(String name, java.util.Set<String> existingIds) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "shop";

        String candidate = base;
        int suffix = 2;
        while (existingIds.contains(candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
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

    public static String identifyPlayer(UUID uuid) {
        IPlayer player = GUIShop.minecraftServer.getPlayerByUUID(uuid);
        return player != null ? player.name().toString() : uuid.toString();
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

    public static ItemStack createDirectionalCompass(
        IPlayer player,
        float relativeYawDegrees
    ) {
        // Player yaw (Minecraft uses degrees, 0 = south, increases clockwise)
        float yaw = player.getYaw();
        float targetYaw = yaw + relativeYawDegrees;

        // Convert yaw to direction vector
        double radians = Math.toRadians(targetYaw);
        double dx = -Math.sin(radians);
        double dz =  Math.cos(radians);

        // Place fake lodestone far away so wobble is minimal
        int distance = 1000;

        BlockPos targetPos = player.getBlockPos().add(
            (int) (dx * distance),
            0,
            (int) (dz * distance)
        );

        ItemStack stack = new ItemStack(Items.COMPASS);

        // Lodestone tracker component (1.21+)
        stack.set(DataComponentTypes.LODESTONE_TRACKER,
            new LodestoneTrackerComponent(
                Optional.of(new GlobalPos(player.getWorldId(), targetPos)),
                false
            )
        );

        return stack;
    }
}
