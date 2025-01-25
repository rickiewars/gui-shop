package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

public class Transaction {

    private final ServerPlayerEntity player;
    private final Shop shop;

    public Transaction(ServerPlayerEntity player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    public boolean buyItem(ShopItem item, boolean tradeMany) {
        EconomyAccount account = EconomyUtils.getAccount(player, shop.getCurrencyId(item));

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
        EconomyAccount account = EconomyUtils.getAccount(player, shop.getCurrencyId(item));

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

        int amountRemovedFromInventory = removeItemsFromInventory(itemToSell, amount, item);
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
        if (sellItem == null) {
            player.sendMessage(Text.literal(
                    "This item cannot be sold in this shop"
            ).formatted(Formatting.RED));
            return false;
        }

        EconomyAccount account = EconomyUtils.getAccount(player, shop.getCurrencyId(sellItem));
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

    private int removeItemsFromInventory (Item itemToRemove, int amount, ShopItem shopItem) {
        //loop to remove items from player's inventory
        PlayerInventory inventory = player.getInventory();
        int amountToSell = amount;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            final int stackCount = stack.getCount();

            if(stack.getItem().equals(itemToRemove) && shopItem.matches(stack)) {
                if (stackCount < amount) {
                    amount -= stackCount;
                    inventory.removeStack(i, stackCount);
                } else if (stackCount > amount) {
                    ItemStack newItem = new ItemStack(itemToRemove, stackCount - amount);
                    newItem.applyChanges(stack.getComponentChanges());
                    inventory.removeStack(i);
                    inventory.setStack(i, newItem);
                    amount = 0;
                    break;
                } else {
                    inventory.removeStack(i);
                    amount = 0;
                    break;
                }
            }
        }
        return amountToSell - amount;
    }



}
