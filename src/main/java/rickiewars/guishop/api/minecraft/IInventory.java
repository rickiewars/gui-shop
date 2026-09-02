package rickiewars.guishop.api.minecraft;

import java.util.function.Predicate;

public interface IInventory {
    int size();

    int count(ResourceId itemId);

    void offerOrDrop(IItemStack stack);

    int remove(ResourceId matchId, int amount, Predicate<IItemStack> filter);

    static int handleRemove(IItemStack slot, ResourceId matchId, int amount, Predicate<IItemStack> filter) {
        if (!slot.itemId().equals(matchId)) return 0;
        if (!filter.test(slot)) return 0;

        int removeCount = Math.min(amount, slot.count());
        slot.decrement(removeCount);

        return removeCount;
    }
}
