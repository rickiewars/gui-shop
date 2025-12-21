package rickiewars.guishop.api.gui;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public interface MenuSlot {
    ItemStack icon();
    void onClick(MenuContext ctx, ClickType clickType);
    List<Text> lore();
    Text name();
}
