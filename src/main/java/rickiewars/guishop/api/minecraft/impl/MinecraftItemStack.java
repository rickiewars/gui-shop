package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MinecraftItemStack implements IItemStack {
    private static final Map<ComponentKey, DataComponentType<?>> COMPONENT_TYPES = new EnumMap<>(ComponentKey.class);
    static {
        COMPONENT_TYPES.put(ComponentKey.DAMAGE, DataComponents.DAMAGE);
        COMPONENT_TYPES.put(ComponentKey.REPAIR_COST, DataComponents.REPAIR_COST);
        COMPONENT_TYPES.put(ComponentKey.CUSTOM_NAME, DataComponents.CUSTOM_NAME);
        COMPONENT_TYPES.put(ComponentKey.LORE, DataComponents.LORE);
    }

    private final ItemStack stack;

    public MinecraftItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack stack() {
        return stack;
    }

    @Override
    public ResourceId itemId() {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ResourceId.of(id.getNamespace(), id.getPath());
    }

    @Override
    public int count() {
        return stack.getCount();
    }

    @Override
    public int maxStackSize() {
        return stack.getMaxStackSize();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public boolean hasComponentChanges() {
        return !stack.getComponentsPatch().isEmpty();
    }

    @Override
    public boolean equalsIgnoringComponents(IItemStack other, Set<ComponentKey> ignored) {
        ItemStack otherStack = unwrap(other);
        if (!stack.getItem().equals(otherStack.getItem())) return false;

        ItemStack a = stack.copy();
        ItemStack b = otherStack.copy();
        for (ComponentKey key : ignored) {
            DataComponentType<?> type = COMPONENT_TYPES.get(key);
            a.remove(type);
            b.remove(type);
        }
        return ItemStack.isSameItemSameComponents(a, b);
    }

    @Override
    public boolean equalsExact(IItemStack other) {
        return ItemStack.isSameItemSameComponents(stack, unwrap(other));
    }

    @Override
    public IItemStack copy() {
        return new MinecraftItemStack(stack.copy());
    }

    @Override
    public IItemStack copyWithCount(int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return new MinecraftItemStack(copy);
    }

    @Override
    public void decrement(int amount) {
        stack.shrink(amount);
    }

    @Override
    public boolean isDamageable() {
        return stack.isDamageableItem();
    }

    @Override
    public int damage() {
        return stack.getDamageValue();
    }

    @Override
    public int maxDamage() {
        return stack.getMaxDamage();
    }

    @Override
    public int repairCost() {
        return stack.getOrDefault(DataComponents.REPAIR_COST, 0);
    }

    @Override
    public boolean componentDiffers(ComponentKey key, IItemStack other) {
        DataComponentType<?> type = COMPONENT_TYPES.get(key);
        return !Objects.equals(stack.get(type), unwrap(other).get(type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftItemStack other)) return false;
        return ItemStack.isSameItemSameComponents(stack, other.stack) && stack.getCount() == other.stack.getCount();
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }

    private static ItemStack unwrap(IItemStack other) {
        if (!(other instanceof MinecraftItemStack mc)) {
            throw new IllegalArgumentException("Expected MinecraftItemStack, got " + other.getClass());
        }
        return mc.stack;
    }
}
