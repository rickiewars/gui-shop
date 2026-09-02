package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;

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
    public int count(ResourceId itemId) {
        int total = 0;
        for (ItemStack s : slots) {
            if (matchesId(s, itemId)) {
                total += s.getCount();
            }
        }
        return total;
    }

    @Override
    public void offerOrDrop(IItemStack stack) {
        ItemStack real = unwrap(stack);
        if (real.isEmpty()) return;

        for (int i = 0; i < SIZE; i++) {
            if (slots[i].isEmpty()) {
                slots[i] = real.copy();
                return;
            }
        }
    }

    @Override
    public int remove(ResourceId matchId, int amount, Predicate<IItemStack> filter) {
        int removed = 0;

        for (int i = 0; i < SIZE && removed < amount; i++) {
            IItemStack slot = new MinecraftItemStack(slots[i]);
            removed += IInventory.handleRemove(slot, matchId, amount - removed, filter);

            if (slots[i].getCount() == 0) {
                slots[i] = ItemStack.EMPTY.copy();
            }
        }

        return removed;
    }

    private static boolean matchesId(ItemStack stack, ResourceId itemId) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id.getNamespace().equals(itemId.namespace()) && id.getPath().equals(itemId.path());
    }

    private static ItemStack unwrap(IItemStack stack) {
        return ((MinecraftItemStack) stack).stack();
    }
}
