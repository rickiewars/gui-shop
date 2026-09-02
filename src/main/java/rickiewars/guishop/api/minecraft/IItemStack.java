package rickiewars.guishop.api.minecraft;

import java.util.Set;

public interface IItemStack {
    /** Stable identity of the item. Adapter maps to/from its registry. */
    ResourceId itemId();

    int count();

    int maxStackSize();

    boolean isEmpty();

    /** True if this stack carries any non-default component data (enchants, custom name, lore, damage, ...). */
    boolean hasComponentChanges();

    /**
     * Component-aware equality, restricted to the given logical component keys.
     * Used by ShopItem.resembles()/matches() to apply graded/flat component policy
     * without core knowing what a "component" actually is on the current MC version.
     */
    boolean equalsIgnoringComponents(IItemStack other, Set<ComponentKey> ignored);

    /** Full item + component equality (no keys ignored). */
    boolean equalsExact(IItemStack other);

    IItemStack copy();

    IItemStack copyWithCount(int count);

    /** Mutates this stack's count in place, e.g. when removing matched items from a live inventory slot. */
    void decrement(int amount);

    boolean isDamageable();

    int damage();

    int maxDamage();

    /** Repair-cost component value, or 0 if absent. */
    int repairCost();

    /** True if the given logical component differs between this stack and other. */
    boolean componentDiffers(ComponentKey key, IItemStack other);

    /**
     * Logical component categories ShopItem's pricing policy cares about. Adapter maps each
     * key to the real ComponentType(s) for the running MC version - core never sees the real type.
     */
    enum ComponentKey {
        DAMAGE,
        REPAIR_COST,
        CUSTOM_NAME,
        LORE
    }
}
