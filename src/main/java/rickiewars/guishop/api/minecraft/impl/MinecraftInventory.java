package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;

import java.util.function.Predicate;

public class MinecraftInventory implements IInventory {
    private final Inventory inv;

    public MinecraftInventory(Inventory inv) {
        this.inv = inv;
    }

    @Override
    public int size() {
        return inv.getContainerSize();
    }

    @Override
    public int count(ResourceId itemId) {
        return inv.countItem(resolveItem(itemId));
    }

    @Override
    public void offerOrDrop(IItemStack stack) {
        inv.placeItemBackInInventory(unwrap(stack));
    }

    @Override
    public int remove(ResourceId matchId, int amount, Predicate<IItemStack> filter) {
        int removed = 0;

        for (int i = 0; i < inv.getContainerSize() && removed < amount; i++) {
            IItemStack slot = new MinecraftItemStack(inv.getItem(i));
            removed += IInventory.handleRemove(slot, matchId, amount - removed, filter);
        }

        return removed;
    }

    private static Item resolveItem(ResourceId itemId) {
        return ItemRegistry.get(itemId);
    }

    private static ItemStack unwrap(IItemStack stack) {
        return ((MinecraftItemStack) stack).stack();
    }
}
