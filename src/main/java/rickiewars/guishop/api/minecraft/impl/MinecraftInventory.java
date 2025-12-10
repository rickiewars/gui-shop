package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rickiewars.guishop.api.minecraft.IInventory;

import java.util.function.Predicate;

public class MinecraftInventory implements IInventory {
    private final PlayerInventory inv;

    public MinecraftInventory(PlayerInventory inv) {
        this.inv = inv;
    }

    @Override
    public int size() {
        return inv.size();
    }

    @Override
    public int count(Item item) {
        return inv.count(item);
    }

    @Override
    public void offerOrDrop(ItemStack stack) {
        inv.offerOrDrop(stack);
    }

    @Override
    public int remove(Item match, int amount, Predicate<ItemStack> filter) {
        int removed = 0;

        for (int i = 0; i < inv.size() && removed < amount; i++) {
            ItemStack slot = inv.getStack(i);
            removed += IInventory.handleRemove(slot, match, amount - removed, filter);
        }

        return removed;
    }
}
