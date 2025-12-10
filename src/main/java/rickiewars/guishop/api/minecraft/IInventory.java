package rickiewars.guishop.api.minecraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface IInventory {
    int size();

    int count(Item item);

    void offerOrDrop(ItemStack stack);

    int remove(Item match, int amount, Predicate<ItemStack> filter);

    static int handleRemove(ItemStack slot, Item match, int amount, Predicate<ItemStack> filter) {
        if (slot.getItem() != match) return 0;
        if (!filter.test(slot)) return 0;

        int removeCount = Math.min(amount, slot.getCount());
        slot.decrement(removeCount);

        return removeCount;
    }
}