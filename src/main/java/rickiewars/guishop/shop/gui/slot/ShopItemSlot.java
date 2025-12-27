package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
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
        ItemStack stack = new ItemStack(
            Registries.ITEM.get(Identifier.of(item.itemId()))
        );
        stack.applyChanges(item.componentChanges());
        return stack;
    }

    public Text name() {
        return Text.literal(item.itemName());
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

        ItemStack cursor = ctx.player().getCursorStack();
        boolean shift = click.shift;
        boolean hasCursor = !cursor.isEmpty();
        boolean matches = hasCursor && item.matches(cursor);

        try {
            if (click.isMiddle) {
                ItemStack result = tx.buyToItemStack(
                    item,
                    cursor,
                    Integer.MAX_VALUE
                );
                ctx.player().setCursorStack(result);
            }
            else if (click.isLeft) {
                ItemStack result;
                if (hasCursor && !matches) {
                    // try selling entire cursor
                    result = tx.sellFromItemStack(cursor, cursor.getCount());
                } else {
                    result = tx.buyToItemStack(
                        item,
                        matches ? cursor : ItemStack.EMPTY,
                        shift ? item.getMaxStackSize() : 1
                    );
                }
                ctx.player().setCursorStack(result);
            }
            else if (click.isRight) {
                if (hasCursor) {
                    ItemStack result = tx.sellFromItemStack(
                        cursor,
                        shift ? cursor.getCount() : 1
                    );
                    ctx.player().setCursorStack(result);
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