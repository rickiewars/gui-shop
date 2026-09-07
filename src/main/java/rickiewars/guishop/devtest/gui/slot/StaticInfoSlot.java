package rickiewars.guishop.devtest.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;

/**
 * A purely decorative slot with a no-op {@code onClick}, exactly like the production
 * {@code BalanceSlot}/{@code PageIndicatorSlot}. Used both for instruction signs (icon + lore as
 * the tooltip text) and as the inert "try to pick this up" test subject -- either way, the point
 * is that clicking it, in any way, must do nothing.
 */
public class StaticInfoSlot implements MenuSlot {
    private final ItemStack icon;
    private final String label;
    private final List<Component> lore;

    public StaticInfoSlot(ItemStack icon, String label, List<Component> lore) {
        this.icon = icon;
        this.label = label;
        this.lore = lore;
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }

    @Override
    public Component name() {
        return Component.literal(label);
    }

    @Override
    public List<Component> lore() {
        return lore;
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {}
}
