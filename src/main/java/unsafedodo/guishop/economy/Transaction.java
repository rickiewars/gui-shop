package unsafedodo.guishop.economy;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;

import java.util.concurrent.ExecutionException;

public class Transaction {

    private final ServerPlayerEntity player;
    private final Shop shop;
    private final boolean suppressMessages;

    public Transaction(ServerPlayerEntity player, Shop shop) { this(player, shop, false); }
    public Transaction(ServerPlayerEntity player, Shop shop, boolean suppressMessages) {
        this.player = player;
        this.shop = shop;
        this.suppressMessages = suppressMessages;
    }

    public boolean buyItem(ShopItem item, boolean tradeMany) {
        int amount = 1;
        ItemStack givenItems = new ItemStack(Registries.ITEM.get(Identifier.of(item.itemId())), amount);
        if (tradeMany) {
            try {
                double balance = GUIShop.economyService.getBalance(player.getUuid());
                int canAfford = (int) (balance / item.buyItemPrice());
                givenItems.setCount(Math.min(canAfford, givenItems.getMaxCount()));
                amount = givenItems.getCount();
            } catch (ExecutionException | InterruptedException ignored) {}
        }

        if (item.hasComponentChanges()) {
            givenItems.applyChanges(item.componentChanges());
        }

        if (amount == 0 || !GUIShop.economyService.remove(player.getUuid(), item.buyItemPrice() * amount)){
            if (!suppressMessages) {
                player.sendMessage(Text.literal(
                        "You don't have enough money"
                ).formatted(Formatting.RED));
            }
            return false;
        }

        player.getInventory().offerOrDrop(givenItems);
        tradeSuccessfulMessage(item, amount, false);
        return true;
    }

    public boolean sellItem(ShopItem item, boolean tradeMany) {
        Item itemToSell = Registries.ITEM.get(Identifier.of(item.itemId()));
        int amount = tradeMany ? Math.min(
                player.getInventory().count(itemToSell),
                itemToSell.getMaxCount()
        ) : 1;

        int amountRemovedFromInventory = removeItemsFromInventory(itemToSell, amount, item);
        if (amountRemovedFromInventory == 0){
            if (!suppressMessages) {
                player.sendMessage(Text.literal(
                        "You don't have this item"
                ).formatted(Formatting.RED));
            }
            return false;
        }

        if (!GUIShop.economyService.add(player.getUuid(), item.sellItemPrice() * amountRemovedFromInventory)) {
            ItemStack refund = new ItemStack(itemToSell, amountRemovedFromInventory);
            refund.applyChanges(item.componentChanges());
            player.getInventory().offerOrDrop(refund);
            if (!suppressMessages) {
                player.sendMessage(Text.literal(
                        "Something went wrong, canceling transaction"
                ).formatted(Formatting.RED));
            }
            return false;
        }

        tradeSuccessfulMessage(item, amountRemovedFromInventory, true);
        return true;
    }

    public boolean sellStack(ItemStack itemStack, boolean tradeMany) {
        int amountInHand = itemStack.getCount();
        int amount = tradeMany ? amountInHand : 1;

        ShopItem sellItem = this.shop.findItem(itemStack);
        if (sellItem == null) {
            if (!suppressMessages) {
                player.sendMessage(Text.literal(
                        "This item cannot be sold in this shop"
                ).formatted(Formatting.RED));
            }
            return false;
        }

        if (!GUIShop.economyService.add(player.getUuid(), sellItem.sellItemPrice() * amount)) {
            if (!suppressMessages) {
                player.sendMessage(Text.literal(
                        "Something went wrong, canceling transaction"
                ).formatted(Formatting.RED));
            }
            return false;
        }

        itemStack.setCount(amountInHand - amount);
        tradeSuccessfulMessage(sellItem, amount, true);
        return true;
    }

    private void tradeSuccessfulMessage(ShopItem item, int amount, boolean isSellTransaction) {
        if (suppressMessages) return;

        String tradeType = isSellTransaction ? "sold" : "bought";
        double price = isSellTransaction ? item.sellItemPrice() : item.buyItemPrice();
        double totalPrice = price * amount;
        MutableText message = Text.literal(String.format(
                "You have %s %d %s for %.2f $",
                tradeType,
                amount,
                item.itemName(),
                totalPrice
        )).formatted(Formatting.GREEN);
        player.sendMessage(message);
    }

    private int removeItemsFromInventory (Item itemToRemove, int amount, ShopItem shopItem) {
        if(player.getInventory().count(itemToRemove) < amount) {
            return 0;
        }

        //loop to remove items from player's inventory
        int amountToSell = amount;
        int i = 0;
        while(amount > 0){
            PlayerInventory inventory = player.getInventory();
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
                } else {
                    inventory.removeStack(i);
                    amount = 0;
                }
            }
            i++;
        }
        return amountToSell - amount;
    }



}
