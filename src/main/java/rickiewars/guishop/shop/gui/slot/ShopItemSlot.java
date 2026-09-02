package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.economy.Transaction;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class ShopItemSlot implements MenuSlot {
    private final Shop shop;
    private final ShopItem item;

    public ShopItemSlot(Shop shop, ShopItem item) {
        this.shop = shop;
        this.item = item;
    }

    public ItemStack icon() {
        return ((MinecraftItemStack) item.stack()).stack().copy();
    }

    public Text name() {
        return Text.literal(item.displayName());
    }

    public List<Text> lore() {
        List<Text> lore = item.getDescriptionAsText();
        lore.add(item.getLoreBuyPrice());
        lore.add(item.getLoreSellPrice());
        lore.add(item.getLoreTradeStackInstruction());
        return lore;
    }

    public void onClick(MenuContext ctx, ClickType click) {
        Transaction tx = new Transaction(ctx.player(), shop);

        ItemStack cursor = ((MinecraftItemStack) ctx.player().getCursorStack()).stack();
        boolean shift = click.shift;
        boolean hasCursor = !cursor.isEmpty();
        boolean itemResemblesCursorItem = hasCursor && item.resembles(new MinecraftItemStack(cursor));

        try {
            if (click.isMiddle) {
                ItemStack result = tx.buyToItemStack(
                    item,
                    cursor,
                    Integer.MAX_VALUE
                );
                ctx.player().setCursorStack(new MinecraftItemStack(result));
            }
            else if (click.isLeft) {
                ItemStack result;
                if (hasCursor && !itemResemblesCursorItem) {
                    // try selling entire cursor
                    result = tx.sellFromItemStack(cursor, cursor.getCount());
                } else {
                    result = tx.buyToItemStack(
                        item,
                        itemResemblesCursorItem ? cursor : ItemStack.EMPTY,
                        shift ? item.getMaxStackSize() : 1
                    );
                }
                ctx.player().setCursorStack(new MinecraftItemStack(result));
            }
            else if (click.isRight) {
                if (hasCursor) {
                    ItemStack result = tx.sellFromItemStack(
                        cursor,
                        shift ? cursor.getCount() : 1
                    );
                    ctx.player().setCursorStack(new MinecraftItemStack(result));
                } else {
                    tx.sellFromInventory(
                        item,
                        shift ? item.getMaxStackSize() : 1
                    );
                }
            }
            ctx.partialRefresh();

        } catch (IllegalStateException e) {
            ctx.player().sendMessage(
                Text.literal(e.getMessage()).formatted(Formatting.RED)
            );
        }
    }
}