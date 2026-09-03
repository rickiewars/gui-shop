package rickiewars.guishop.api.gui;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface MenuSlot {
    ItemStack icon();
    void onClick(MenuContext ctx, ClickType clickType);
    List<Component> lore();
    Component name();
}
