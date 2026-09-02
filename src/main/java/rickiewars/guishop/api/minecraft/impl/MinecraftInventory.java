package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;

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
    public int count(ResourceId itemId) {
        return inv.count(resolveItem(itemId));
    }

    @Override
    public void offerOrDrop(IItemStack stack) {
        inv.offerOrDrop(unwrap(stack));
    }

    @Override
    public int remove(ResourceId matchId, int amount, Predicate<IItemStack> filter) {
        int removed = 0;

        for (int i = 0; i < inv.size() && removed < amount; i++) {
            IItemStack slot = new MinecraftItemStack(inv.getStack(i));
            removed += IInventory.handleRemove(slot, matchId, amount - removed, filter);
        }

        return removed;
    }

    private static Item resolveItem(ResourceId itemId) {
        return Registries.ITEM.get(Identifier.of(itemId.namespace(), itemId.path()));
    }

    private static ItemStack unwrap(IItemStack stack) {
        return ((MinecraftItemStack) stack).stack();
    }
}
