package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;
import java.util.function.Consumer;

public class BackSlot implements MenuSlot {
    private final Consumer<MenuContext> onGoBack;

    public BackSlot(Consumer<MenuContext> onGoBack) {
        this.onGoBack = onGoBack;
    }

    public ItemStack icon() {
        return Items.DARK_OAK_DOOR.getDefaultInstance();
    }

    public Component name() {
        return Component.literal("Back").withStyle(ChatFormatting.YELLOW);
    }

    public List<Component> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {
        onGoBack.accept(ctx);
    }
}
