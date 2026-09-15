package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import rickiewars.guishop.api.minecraft.ResourceId;

import java.util.Optional;

public interface ItemRegistry {

    static ResourceId idOf(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return ResourceId.of(id.getNamespace(), id.getPath());
    }

    static Item get(ResourceId id) {
        //? if >=1.21.2 {
        return BuiltInRegistries.ITEM.getValue(id.toIdentifier());
        //?} else {
        /*return BuiltInRegistries.ITEM.get(id.toResourceLocation());
        *///?}
    }

    static Optional<Item> getOptional(ResourceId id) {
        return BuiltInRegistries.ITEM.getOptional(id.toIdentifier());
    }

    static Item get(ResourceId id, Item defaultItem) {
        return getOptional(id).orElse(defaultItem);
    }

    static boolean contains(ResourceId id) {
        return BuiltInRegistries.ITEM.containsKey(id.toIdentifier());
    }
}
