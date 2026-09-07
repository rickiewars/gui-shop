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

/** Gives the player a clearly-marked dummy item in their real inventory, on any click. */
public class GiveMarkerItemSlot implements MenuSlot {
    public static final String MARKER_NAME = "SGUI Test Marker";

    @Override
    public ItemStack icon() {
        return markerStack();
    }

    @Override
    public Component name() {
        return Component.literal("Get marker item").withStyle(ChatFormatting.GREEN);
    }

    @Override
    public List<Component> lore() {
        return List.of(
            Component.literal("Click to receive a \"" + MARKER_NAME + "\" in your real inventory.")
        );
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {
        ctx.player().giveItem(new MinecraftItemStack(markerStack()));
    }

    private static ItemStack markerStack() {
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(MARKER_NAME).withStyle(s -> s.withItalic(false)));
        return stack;
    }
}
