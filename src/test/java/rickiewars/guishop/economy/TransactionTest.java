package rickiewars.guishop.economy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest extends EconomyTest {

    TestPlayer player;
    Shop shop;
    Transaction transaction;

    ShopItem shopItem;
    Item item;

    @BeforeEach
    void setup() {
        player = new TestPlayer(UUID.randomUUID());

        shop = TestUtils.testShop(
            List.of("minecraft:stone"),
            economy.currencyCreditsId,
            10
        );

        player.addDefaultAccount(shop.getDefaultCurrencyId());
        player.getAccount(economy.currencyCreditsId).setBalance(1_000);

        shopItem = shop.getItems().getFirst();
        item = Registries.ITEM.get(Identifier.of(shopItem.itemId()));

        transaction = new Transaction(player, shop);
    }

    // -------------------------------------------------------------------------
    // Buy to Inventory
    // -------------------------------------------------------------------------

    @Test
    void buyToInventoryAddsStackToInventory() {
        transaction.buyToInventory(shopItem, 10);

        assertEquals(10, player.getInventory().count(item));
        assertEquals(900, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToInventoryDoesNotBuyMoreThanCanAfford() {
        player.getAccount(economy.currencyCreditsId).setBalance(505);

        transaction.buyToInventory(shopItem, 64);

        assertEquals(50, player.getInventory().count(item));
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToInventoryDoesNothingIfAmountNegative() {
        player.giveItem(new ItemStack(item, 5));

        transaction.buyToInventory(shopItem, -5);

        assertEquals(5, player.getInventory().count(item));
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    // -------------------------------------------------------------------------
    // Buy to Item Stack
    // -------------------------------------------------------------------------

    @Test
    void buyToItemStackWithEmptyItemCreatesNewStack() {
        ItemStack result = transaction.buyToItemStack(
            shopItem,
            ItemStack.EMPTY,
            5
        );

        assertEquals(5, result.getCount());
        assertEquals(item, result.getItem());
        assertEquals(950, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToItemStackAddsToMatchingItemStackStack() {
        ItemStack cursor = new ItemStack(item, 3);

        ItemStack result = transaction.buyToItemStack(
            shopItem,
            cursor,
            5
        );

        assertEquals(8, result.getCount());
        assertEquals(item, result.getItem());
        assertEquals(950, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToItemStackThrowsIfItemStackDoesNotMatch() {
        ItemStack cursor = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:dirt")), 1);

        assertThrows(
            IllegalStateException.class,
            () -> transaction.buyToItemStack(shopItem, cursor, 1)
        );
    }

    @Test
    void buyToItemStackDoesNotOverflow() {
        ItemStack cursor = new ItemStack(item, 60);

        ItemStack result = transaction.buyToItemStack(
            shopItem,
            cursor,
            10
        );

        assertEquals(64, result.getCount());
        assertEquals(item, result.getItem());
        assertEquals(960, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToItemStackDoesNotBuyMoreThanCanAfford() {
        ItemStack cursor = new ItemStack(item, 5);
        player.getAccount(economy.currencyCreditsId).setBalance(505);

        ItemStack result = transaction.buyToItemStack(
            shopItem,
            cursor,
            64
        );

        assertEquals(55, result.getCount());
        assertEquals(item, result.getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToItemStackDoesNothingIfAmountNegative() {
        ItemStack cursor = new ItemStack(item, 5);

        ItemStack result = transaction.buyToItemStack(
            shopItem,
            cursor,
            -5
        );

        assertEquals(5, result.getCount());
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    // -------------------------------------------------------------------------
    // Sell from inventory
    // -------------------------------------------------------------------------

    @Test
    void sellFromInventoryRemovesItemsAndPaysPlayer() {
        player.giveItem(new ItemStack(item, 5));

        transaction.sellFromInventory(shopItem, 3);

        assertEquals(2, player.getInventory().count(item));
        assertEquals(1_030, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryDoesNothingIfNoItems() {
        transaction.sellFromInventory(shopItem, 5);

        assertEquals(0, player.getInventory().count(item));
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryOnlySellsTheItemAmountTheyOwn() {
        player.giveItem(new ItemStack(item, 5));

        transaction.sellFromInventory(shopItem, 10);

        assertEquals(0, player.getInventory().count(item));
        assertEquals(1_050, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryDoesNothingIfAmountNegative() {
        player.giveItem(new ItemStack(item, 5));

        transaction.sellFromInventory(shopItem, -5);

        assertEquals(5, player.getInventory().count(item));
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    // -------------------------------------------------------------------------
    // Sell from Item Stack
    // -------------------------------------------------------------------------

    @Test
    void sellFromItemStackRemovesItemsAmountFromStackAndPaysPlayer() {
        ItemStack cursor = new ItemStack(item, 4);

        ItemStack result = transaction.sellFromItemStack(cursor, 2);

        assertEquals(2, result.getCount());
        assertEquals(1_020, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromItemStackDoesNotPayPlayerMoreThanAmountInStack() {
        ItemStack cursor = new ItemStack(item, 5);

        ItemStack result = transaction.sellFromItemStack(cursor, 64);

        assertEquals(0, result.getCount());
        assertEquals(1_050, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromItemStackDoesNothingIfItemNotInShop() {
        ItemStack cursor = new ItemStack(
            Registries.ITEM.get(Identifier.of("minecraft:dirt")),
            5
        );

        ItemStack result = transaction.sellFromItemStack(cursor, 5);

        assertSame(cursor, result);
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromItemStackDoesNothingIfAmountNegative() {
        ItemStack cursor = new ItemStack(item, 5);

        ItemStack result = transaction.sellFromItemStack(cursor, -5);

        assertSame(cursor, result);
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }
}

