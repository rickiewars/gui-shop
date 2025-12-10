package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rickiewars.guishop.api.minecraft.IInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TestInventory implements IInventory {
    private static final int SIZE = 36; // standard survival inventory size
    private final ItemStack[] slots = new ItemStack[SIZE];

    public TestInventory() {
        // initialize all slots to EMPTY
        for (int i = 0; i < SIZE; i++) {
            slots[i] = ItemStack.EMPTY.copy();
        }
    }

    @Override
    public int size() {
        return SIZE;
    }

    public void setStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE) return;
        slots[slot] = stack == null ? ItemStack.EMPTY.copy() : stack.copy();
    }

    public void addStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        for (int i = 0; i < SIZE; i++) {
            if (slots[i].isEmpty()) {
                slots[i] = stack.copy();
                return;
            }
        }
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            slots[i] = ItemStack.EMPTY.copy();
        }
    }

    public List<ItemStack> snapshot() {
        List<ItemStack> list = new ArrayList<>(SIZE);
        Collections.addAll(list, slots);
        return Collections.unmodifiableList(list);
    }

    @Override
    public int count(Item item) {
        int total = 0;
        for (ItemStack s : slots) {
            if (s.getItem().equals(item)) {
                total += s.getCount();
            }
        }
        return total;
    }

    @Override
    public void offerOrDrop(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        for (int i = 0; i < SIZE; i++) {
            if (slots[i].isEmpty()) {
                slots[i] = stack.copy();
                return;
            }
        }
    }

    @Override
    public int remove(Item match, int amount, Predicate<ItemStack> filter) {
        int removed = 0;

        for (int i = 0; i < SIZE && removed < amount; i++) {
            ItemStack slot = slots[i];
            removed += IInventory.handleRemove(slot, match, amount - removed, filter);

            if (slot.getCount() == 0) {
                slots[i] = ItemStack.EMPTY.copy();
            }
        }

        return removed;
    }
}
