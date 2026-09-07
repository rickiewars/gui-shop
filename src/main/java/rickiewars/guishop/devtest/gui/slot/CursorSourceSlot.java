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
 * Mirrors the production {@code ShopItemSlot}'s full click matrix (left/shift-left/middle grow the
 * cursor stack, right/shift-right shrink it) on a plain dummy item, with no shop/economy involved --
 * isolates exactly the cursor read/write round-trip and click-type detection the real slot depends
 * on. See the dev-test menu's instructions for the full list of other click types (number keys, drop,
 * offhand swap, double-click, drag) to also try here.
 */
public class CursorSourceSlot implements MenuSlot {
    private static final int MAX_STACK = 64;

    @Override
    public ItemStack icon() {
        return new ItemStack(Items.NETHER_STAR);
    }

    @Override
    public Component name() {
        return Component.literal("Cursor source").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public List<Component> lore() {
        return List.of(
            Component.literal("Left click: +1 to cursor"),
            Component.literal("Shift-left / middle click: fill cursor to 64"),
            Component.literal("Right click: -1 from cursor"),
            Component.literal("Shift-right click: empty the cursor"),
            Component.literal("Also try: number keys 1-9, drop, ctrl+drop, offhand"),
            Component.literal("swap, double click, and dragging -- none of these"),
            Component.literal("should duplicate the star or leak it into your"),
            Component.literal("real inventory/hotbar/offhand or drop it on the ground.")
        );
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {
        ItemStack cursor = ((MinecraftItemStack) ctx.player().getCursorStack()).stack();
        boolean holdingOurs = !cursor.isEmpty() && cursor.getItem() == Items.NETHER_STAR;
        int current = holdingOurs ? cursor.getCount() : 0;

        if (click.isMiddle) {
            setCursor(ctx, MAX_STACK);
        } else if (click.isLeft) {
            if (!cursor.isEmpty() && !holdingOurs) return;
            setCursor(ctx, click.shift ? MAX_STACK : Math.min(MAX_STACK, current + 1));
        } else if (click.isRight) {
            if (!holdingOurs) return;
            setCursor(ctx, click.shift ? 0 : Math.max(0, current - 1));
        }
    }

    private void setCursor(MenuContext ctx, int count) {
        ItemStack result = count <= 0 ? ItemStack.EMPTY : new ItemStack(Items.NETHER_STAR, count);
        ctx.player().setCursorStack(new MinecraftItemStack(result));
    }
}
