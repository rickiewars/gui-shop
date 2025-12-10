package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.minecraft.IInventory;
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

    public boolean buyItem(ShopItem item, boolean tradeMany) {
        if (item.buyItemPrice() < 0) {
            player.sendMessage(Text.literal(
                "This item cannot be bought in this shop"
            ).formatted(Formatting.RED));
            return false;
        }

        EconomyAccount account = player.getAccount(shop.getCurrencyId(item));

        int amount = 1;
        ItemStack givenItems = new ItemStack(Registries.ITEM.get(Identifier.of(item.itemId())), amount);
        if (tradeMany) {
            long balance = account.balance();
            int canAfford = (int) (balance / item.buyItemPrice());
            givenItems.setCount(Math.min(canAfford, givenItems.getMaxCount()));
            amount = givenItems.getCount();
        }

        if (item.hasComponentChanges()) {
            givenItems.applyChanges(item.componentChanges());
        }

        if (amount == 0 || account.decreaseBalance(item.buyItemPrice() * amount).isFailure()){
            player.sendMessage(Text.literal(
                    "You don't have enough money"
            ).formatted(Formatting.RED));
            return false;
        }

        player.getInventory().offerOrDrop(givenItems);
        tradeSuccessfulMessage(item, amount, false);
        return true;
    }

    public boolean sellItem(ShopItem item, boolean tradeMany) {
        if (item.sellItemPrice() < 0) {
            player.sendMessage(Text.literal(
                "This item cannot be sold in this shop"
            ).formatted(Formatting.RED));
            return false;
        }

        EconomyAccount account = player.getAccount(shop.getCurrencyId(item));

        Item itemToSell = Registries.ITEM.get(Identifier.of(item.itemId()));
        int amount = tradeMany ? Math.min(
                player.getInventory().count(itemToSell),
                itemToSell.getMaxCount()
        ) : 1;

        EconomyTransaction canIncreaseBalance = account.canIncreaseBalance(item.sellItemPrice() * amount);
        if (canIncreaseBalance.isFailure()) {
            player.sendMessage(canIncreaseBalance.message());
            return false;
        }

        IInventory inventory = player.getInventory();
        int amountRemovedFromInventory = inventory.remove(itemToSell, amount, item::matches);
        if (amountRemovedFromInventory == 0){
            player.sendMessage(Text.literal(
                    "You don't have this item"
            ).formatted(Formatting.RED));
            return false;
        }

        account.increaseBalance(item.sellItemPrice() * amountRemovedFromInventory);
        tradeSuccessfulMessage(item, amountRemovedFromInventory, true);
        return true;
    }

    public boolean sellStack(ItemStack itemStack, boolean tradeMany) {
        int amountInHand = itemStack.getCount();
        int amount = tradeMany ? amountInHand : 1;

        ShopItem sellItem = this.shop.findItem(itemStack);
        if (sellItem == null || sellItem.sellItemPrice() < 0) {
            player.sendMessage(Text.literal(
                    "This item cannot be sold in this shop"
            ).formatted(Formatting.RED));
            return false;
        }

        EconomyAccount account = player.getAccount(shop.getCurrencyId(sellItem));
        EconomyTransaction transaction = account.increaseBalance(sellItem.sellItemPrice() * amount);
        if (transaction.isFailure()) {
            player.sendMessage(transaction.message());
            return false;
        }

        itemStack.setCount(amountInHand - amount);
        tradeSuccessfulMessage(sellItem, amount, true);
        return true;
    }

    private void tradeSuccessfulMessage(ShopItem item, int amount, boolean isSellTransaction) {
        String tradeType = isSellTransaction ? "sold" : "bought";
        long price = isSellTransaction ? item.sellItemPrice() : item.buyItemPrice();
        long totalPrice = price * amount;
        MutableText message = Text.literal(String.format(
                "You have %s %d %s for %s",
                tradeType,
                amount,
                item.itemName(),
                item.formatCurrency(totalPrice)
        )).formatted(Formatting.GREEN);
        player.sendMessage(message);
    }
}
