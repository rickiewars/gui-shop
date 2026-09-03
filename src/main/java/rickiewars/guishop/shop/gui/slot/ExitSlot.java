package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;

public class ExitSlot implements MenuSlot {
    public ItemStack icon() {
        return Items.BARRIER.getDefaultInstance();
    }

    public Component name() {
        return Component.literal("Exit");
    }

    public List<Component> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {
        ctx.close();
    }
}