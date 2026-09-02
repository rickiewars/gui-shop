package rickiewars.guishop.economy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
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
        item = Registries.ITEM.get(shopItem.itemId());

        transaction = new Transaction(player, shop);
    }

    private static ResourceId idOf(Item item) {
        return new MinecraftItemStack(new ItemStack(item)).itemId();
    }

    // -------------------------------------------------------------------------
    // Buy to Inventory
    // -------------------------------------------------------------------------

    @Test
    void buyToInventoryAddsStackToInventory() {
        transaction.buyToInventory(shopItem, 10);

        assertEquals(10, player.getInventory().count(idOf(item)));
        assertEquals(900, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToInventoryDoesNotBuyMoreThanCanAfford() {
        player.getAccount(economy.currencyCreditsId).setBalance(505);

        transaction.buyToInventory(shopItem, 64);

        assertEquals(50, player.getInventory().count(idOf(item)));
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void buyToInventoryDoesNothingIfAmountNegative() {
        player.giveItem(new MinecraftItemStack(new ItemStack(item, 5)));

        transaction.buyToInventory(shopItem, -5);

        assertEquals(5, player.getInventory().count(idOf(item)));
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
        player.giveItem(new MinecraftItemStack(new ItemStack(item, 5)));

        transaction.sellFromInventory(shopItem, 3);

        assertEquals(2, player.getInventory().count(idOf(item)));
        assertEquals(1_030, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryDoesNothingIfNoItems() {
        transaction.sellFromInventory(shopItem, 5);

        assertEquals(0, player.getInventory().count(idOf(item)));
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryOnlySellsTheItemAmountTheyOwn() {
        player.giveItem(new MinecraftItemStack(new ItemStack(item, 5)));

        transaction.sellFromInventory(shopItem, 10);

        assertEquals(0, player.getInventory().count(idOf(item)));
        assertEquals(1_050, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryDoesNothingIfAmountNegative() {
        player.giveItem(new MinecraftItemStack(new ItemStack(item, 5)));

        transaction.sellFromInventory(shopItem, -5);

        assertEquals(5, player.getInventory().count(idOf(item)));
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void sellFromInventoryDoesNotSellDamagedItem() {
        Item pickaxe = Registries.ITEM.get(Identifier.of("minecraft:netherite_pickaxe"));
        ShopItem pickaxeShopItem = new ShopItem(
            "My faforite pickaxe",
            new MinecraftItemStack(new ItemStack(pickaxe)),
            10,
            100,
            economy.currencyCreditsId,
            List.of()
        );

        ItemStack damaged = new ItemStack(pickaxe);
        damaged.setDamage(125);
        player.giveItem(new MinecraftItemStack(damaged));

        transaction.sellFromInventory(pickaxeShopItem, 1);

        assertEquals(
            1,
            player.getInventory().count(idOf(pickaxe)),
            "I don't want to accedently sell my favorite pickaxe!"
        );
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
    void sellFromItemStackPaysAdjustedAmountForDamagedItem() {
        if (eu.pb4.common.economy.api.CommonEconomy.getProvider(GuiShopEconomyProvider.ID) == null) {
            GuiShopEconomyProvider.init();
        }

        Item pickaxe = Registries.ITEM.get(Identifier.of("minecraft:iron_pickaxe"));
        ShopItem pickaxeShopItem = new ShopItem(
            "Pickaxe",
            new MinecraftItemStack(new ItemStack(pickaxe)),
            10,
            100,
            economy.currencyCreditsId,
            List.of()
        );
        var pickaxeShop = new Shop("pickaxe_shop", "Pickaxe Shop", List.of(pickaxeShopItem), economy.currencyCreditsId);
        var tx = new Transaction(player, pickaxeShop);

        ItemStack damaged = new ItemStack(pickaxe);
        damaged.setDamage(125);

        tx.sellFromItemStack(damaged, 1);

        assertTrue(player.getAccount(economy.currencyCreditsId).balance() < 1_000 + pickaxeShopItem.sellPrice());
        assertTrue(player.getReceivedMessages().stream()
            .anyMatch(text -> text.getString().contains("adjusted for condition")));
    }

    @Test
    void sellFromItemStackDoesNothingIfAmountNegative() {
        ItemStack cursor = new ItemStack(item, 5);

        ItemStack result = transaction.sellFromItemStack(cursor, -5);

        assertSame(cursor, result);
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    void buyThrowsErrorIfNotEnoughMoney() {
        player.getInventory().offerOrDrop(
            new MinecraftItemStack(new ItemStack(item, 5))
        );
        player.getAccount(economy.currencyCreditsId).setBalance(1);

        var ex = assertThrows(
            IllegalStateException.class, () -> transaction.buyToInventory(
                shopItem,
                1
            ));
        assertEquals("Not enough money", ex.getMessage());
        assertEquals(1, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(5, player.getInventory().count(idOf(item)));
    }

    @Test
    void buyToInventoryThrowsErrorIfNotBuyable() {
        player.getInventory().offerOrDrop(
            new MinecraftItemStack(new ItemStack(item, 5))
        );

        var nonBuyableItem = new ShopItem(
            "Non-buyable Item",
            new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
            -1,
            10,
            null,
            List.of()
        );

        var ex = assertThrows(
            IllegalStateException.class, () -> transaction.buyToInventory(
                nonBuyableItem,
                1
            ));
        assertEquals("Not buyable", ex.getMessage());
        assertEquals(5, player.getInventory().count(idOf(item)));
    }

    @Test
    void buyToItemStackThrowsErrorIfNotBuyable() {
        ItemStack cursor = new ItemStack(
            Registries.ITEM.get(Identifier.of("minecraft:stone")),
            5
        );

        var nonBuyableItem = new ShopItem(
            "Non-buyable Item",
            new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
            -1,
            10,
            null,
            List.of()
        );

        var shop = new Shop("test_shop", "Test Shop", List.of(
            nonBuyableItem
        ), economy.currencyCreditsId);

        var tx = new Transaction(player, shop);

        var ex = assertThrows(
            IllegalStateException.class, () -> tx.buyToItemStack(
                nonBuyableItem,
                cursor,
                1
            ));
        assertEquals("Not buyable", ex.getMessage());
        assertEquals(5, cursor.getCount());
    }

    @Test
    void sellFromInventoryThrowsErrorIfNotSellable() {
        player.getInventory().offerOrDrop(
            new MinecraftItemStack(new ItemStack(item, 5))
        );

        var nonSellableItem = new ShopItem(
            "Non-sellable Item",
            new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
            10,
            -1,
            null,
            List.of()
        );

        var ex = assertThrows(
            IllegalStateException.class, () -> transaction.sellFromInventory(
                nonSellableItem,
                1
            ));

        assertEquals("Not sellable", ex.getMessage());
        assertEquals(5, player.getInventory().count(idOf(item)));
    }

    @Test
    void sellFromItemStackDoesNothingWhenListingIsNotSellable() {
        ItemStack cursor = new ItemStack(
            Registries.ITEM.get(Identifier.of("minecraft:stone")),
            5
        );

        var nonSellableItem = new ShopItem(
            "Non-sellable Item",
            new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
            10,
            -1,
            null,
            List.of()
        );

        var shop = new Shop("test_shop", "Test Shop", List.of(
            nonSellableItem
        ), economy.currencyCreditsId);

        var tx = new Transaction(player, shop);

        ItemStack result = tx.sellFromItemStack(cursor, 1);

        assertSame(cursor, result);
        assertEquals(5, cursor.getCount());
        assertEquals(1_000, player.getAccount(economy.currencyCreditsId).balance());
    }
}

