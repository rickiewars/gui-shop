package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import rickiewars.guishop.api.minecraft.ResourceId;

import java.util.Optional;

public final class ItemRegistry {
    private ItemRegistry() {}

    public static ResourceId idOf(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return ResourceId.of(id.getNamespace(), id.getPath());
    }

    public static Item get(ResourceId id) {
        //? if >=1.21.2 {
        return BuiltInRegistries.ITEM.getValue(id.toIdentifier());
        //?} else {
        /*return BuiltInRegistries.ITEM.get(id.toResourceLocation());
        *///?}
    }

    public static Optional<Item> getOptional(ResourceId id) {
        return BuiltInRegistries.ITEM.getOptional(id.toIdentifier());
    }

    public static boolean contains(ResourceId id) {
        return BuiltInRegistries.ITEM.containsKey(id.toIdentifier());
    }
}
