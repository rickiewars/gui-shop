package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
        if (item.buyItemPrice() < 0) throw new IllegalStateException("Not buyable");

        int bought = pay(item, amount);
        ItemStack stack = createStack(item);
        stack.setCount(bought);
        player.getInventory().offerOrDrop(stack);
    }

    public ItemStack buyToItemStack(ShopItem item, ItemStack itemStack, int amount) {
        if (amount <= 0) return itemStack;
        if (item.buyItemPrice() < 0) throw new IllegalStateException("Not buyable");

        ItemStack base = createStack(item);
        int existing = 0;
        if (!itemStack.isEmpty()) {
            if (!item.matches(itemStack)) {
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
        if (item.sellItemPrice() < 0) throw new IllegalStateException("Not sellable");

        int removed = player.getInventory().remove(
            getItem(item),
            amount,
            item::matches
        );
        if (removed > 0) earn(item, removed);
    }

    public ItemStack sellFromItemStack(ItemStack itemStack, int amount) {
        if (amount <= 0) return itemStack;

        ShopItem shopItem = shop.findItem(itemStack);
        if (shopItem == null) return itemStack;
        if (shopItem.sellItemPrice() < 0) throw new IllegalStateException("Not sellable");

        int toSell = Math.min(amount, itemStack.getCount());
        earn(shopItem, toSell);

        itemStack.setCount(itemStack.getCount() - toSell);
        return itemStack;
    }

    private int pay(ShopItem item, int amount) {
        EconomyAccount acc = player.getAccount(shop.getCurrencyId(item));
        int canAfford = Math.min(
            (int) (acc.balance() / item.buyItemPrice()),
            amount
        );
        long cost = item.buyItemPrice() * canAfford;

        if (canAfford == 0 || acc.decreaseBalance(cost).isFailure()) {
            throw new IllegalStateException("Not enough money");
        }
        return canAfford;
    }

    private void earn(ShopItem item, int amount) {
        EconomyAccount acc = player.getAccount(shop.getCurrencyId(item));
        acc.increaseBalance(item.sellItemPrice() * amount);
    }

    private ItemStack createStack(ShopItem item) {
        ItemStack stack = new ItemStack(getItem(item));
        if (item.hasComponentChanges()) stack.applyChanges(item.componentChanges());
        return stack;
    }

    private Item getItem(ShopItem item) {
        return Registries.ITEM.get(Identifier.of(item.itemId()));
    }
}
