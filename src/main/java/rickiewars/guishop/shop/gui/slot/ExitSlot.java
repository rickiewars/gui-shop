package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;

public class ExitSlot implements MenuSlot {
    public ItemStack icon() {
        return Items.BARRIER.getDefaultStack();
    }

    public Text name() {
        return Text.literal("Exit");
    }

    public List<Text> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {
        ctx.close();
    }
}