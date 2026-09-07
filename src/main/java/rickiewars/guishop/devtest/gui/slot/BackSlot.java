package rickiewars.guishop.devtest.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;
import java.util.function.Supplier;

/** Returns to the dev-test root menu (as opposed to {@code ExitSlot}, which closes the GUI outright). */
public class BackSlot implements MenuSlot {
    private final Supplier<Menu> target;

    public BackSlot(Supplier<Menu> target) {
        this.target = target;
    }

    @Override
    public ItemStack icon() {
        return Items.ARROW.getDefaultInstance();
    }

    @Override
    public Component name() {
        return Component.literal("Back").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public List<Component> lore() {
        return List.of();
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {
        ctx.open(target.get());
    }
}
