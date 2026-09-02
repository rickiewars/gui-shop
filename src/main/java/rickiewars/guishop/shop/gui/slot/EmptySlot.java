package rickiewars.guishop.shop.gui.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.economy.Transaction;
import rickiewars.guishop.shop.Shop;

import java.util.List;

public class EmptySlot implements MenuSlot {
    private final Shop shop;
    public EmptySlot(Shop shop) {
        this.shop = shop;
    }

    public ItemStack icon() {
        return net.minecraft.item.Items.AIR.getDefaultStack();
    }

    public Text name() {
        return Text.empty();
    }

    public List<Text> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, eu.pb4.sgui.api.ClickType click) {
        ItemStack cursor = ((MinecraftItemStack) ctx.player().getCursorStack()).stack();
        if (cursor.isEmpty()) return;

        Transaction tx = new Transaction(ctx.player(), shop);
        boolean shift = click.shift;

        try {
            if (click.isRight || click.isLeft) {
                ItemStack result = tx.sellFromItemStack(
                    cursor,
                    shift || click.isLeft ? cursor.getCount() : 1
                );
                ctx.player().setCursorStack(new MinecraftItemStack(result));

                ctx.partialRefresh();
            }
        } catch (IllegalStateException e) {
            ctx.player().sendMessage(
                Text.literal(e.getMessage()).formatted(Formatting.RED)
            );
        }
    }
}