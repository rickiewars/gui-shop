package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

public class Transaction {

    private final IPlayer player;
    private final Shop shop;

    public Transaction(IPlayer player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }


    public void buyToInventory(ShopItem item, int amount) {
        if (amount <= 0) return;
        if (item.buyPrice() < 0) throw new IllegalStateException("Not buyable");

        int bought = pay(item, amount);
        ItemStack stack = createStack(item);
        stack.setCount(bought);
        player.getInventory().offerOrDrop(stack);
    }

    public ItemStack buyToItemStack(ShopItem item, ItemStack itemStack, int amount) {
        if (amount <= 0) return itemStack;
        if (item.buyPrice() < 0) throw new IllegalStateException("Not buyable");

        ItemStack base = createStack(item);
        int existing = 0;
        if (!itemStack.isEmpty()) {
            if (!item.resembles(itemStack)) {
                throw new IllegalStateException("Stack does not match shop item");
            }
            existing = itemStack.getCount();
        }

        int maxAdd = base.getMaxCount() - existing;
        int toBuy = Math.min(amount, maxAdd);

        if (toBuy <= 0) return itemStack;

        int bought = pay(item, toBuy);

        base.setCount(existing + bought);
        return base;
    }

    public void sellFromInventory(ShopItem item, int amount) {
        if (amount <= 0) return;
        if (item.sellPrice() < 0) throw new IllegalStateException("Not sellable");

        int removed = player.getInventory().remove(
            getItem(item),
            amount,
            item::matches
        );
        if (removed > 0) earn(item, item.sellPrice() * removed, false);
    }

    public ItemStack sellFromItemStack(ItemStack itemStack, int amount) {
        if (amount <= 0) return itemStack;

        ShopItem shopItem = shop.findHighestPayingItem(itemStack);
        if (shopItem == null) return itemStack;
        if (shopItem.sellPrice() < 0) throw new IllegalStateException("Not sellable");

        int toSell = Math.min(amount, itemStack.getCount());
        long payoutPerUnit = shop.getSellPricing().adjustedPayout(shopItem, itemStack);
        boolean adjusted = payoutPerUnit != shopItem.sellPrice();
        earn(shopItem, payoutPerUnit * toSell, adjusted);

        itemStack.setCount(itemStack.getCount() - toSell);
        return itemStack;
    }

    private int pay(ShopItem item, int amount) {
        EconomyAccount acc = player.getAccount(shop.getCurrencyId(item));
        int canAfford = Math.min(
            (int) (acc.balance() / item.buyPrice()),
            amount
        );
        long cost = item.buyPrice() * canAfford;

        if (canAfford == 0 || acc.decreaseBalance(cost).isFailure()) {
            throw new IllegalStateException("Not enough money");
        }
        return canAfford;
    }

    private void earn(ShopItem item, long amount, boolean adjustedForCondition) {
        EconomyAccount acc = player.getAccount(shop.getCurrencyId(item));
        acc.increaseBalance(amount);

        if (adjustedForCondition) {
            player.sendMessage(Text.literal("Sold for " + item.formatCurrency(amount) + " (adjusted for condition)")
                .formatted(Formatting.GRAY));
        }
    }

    private ItemStack createStack(ShopItem item) {
        return item.stack().copy();
    }

    private Item getItem(ShopItem item) {
        return item.stack().getItem();
    }
}
