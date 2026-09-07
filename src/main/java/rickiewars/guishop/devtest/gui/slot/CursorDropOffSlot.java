package rickiewars.guishop.devtest.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;

import java.util.List;

/**
 * Mirrors the production {@code EmptySlot}'s click matrix on a *different* slot than the one that
 * put the item on the cursor -- left click (or shift-right) returns the whole cursor stack to the
 * player's real inventory, right click returns just 1. Proves the cursor is genuinely global
 * player state, not tied to the slot instance that last wrote it.
 */
public class CursorDropOffSlot implements MenuSlot {
    @Override
    public ItemStack icon() {
        return new ItemStack(Items.HOPPER);
    }

    @Override
    public Component name() {
        return Component.literal("Cursor drop-off").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public List<Component> lore() {
        return List.of(
            Component.literal("With something on your cursor (see Cursor source):"),
            Component.literal("Left / shift-right click: return it all to your inventory"),
            Component.literal("Right click: return 1 to your inventory")
        );
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {
        ItemStack cursor = ((MinecraftItemStack) ctx.player().getCursorStack()).stack();
        if (cursor.isEmpty()) return;
        if (!click.isLeft && !click.isRight) return;

        int giveCount = (click.shift || click.isLeft) ? cursor.getCount() : 1;

        ItemStack toGive = cursor.copy();
        toGive.setCount(giveCount);
        ctx.player().giveItem(new MinecraftItemStack(toGive));

        int remaining = cursor.getCount() - giveCount;
        ItemStack result = remaining <= 0 ? ItemStack.EMPTY : new ItemStack(cursor.getItem(), remaining);
        ctx.player().setCursorStack(new MinecraftItemStack(result));
    }
}
