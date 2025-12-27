package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ItemSlotTest extends EconomyTest {
    TestMenuController controller;
    MenuContext ctx;
    ShopMenu menu;
    TestPlayer player;
    Item matchingItem;
    int matchingItemIndex = 0;
    int nonMatchingItemIndex = 1;

    @BeforeEach
    void setup() {
        player = new TestPlayer(UUID.randomUUID());
        matchingItem = Items.STONE;
        Shop shop = TestUtils.testShop(List.of(
            Registries.ITEM.getId(matchingItem).toString(),
            Registries.ITEM.getId(Items.COBBLESTONE).toString()
        ), economy.currencyCreditsId,5);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        controller = new TestMenuController();
        menu = new ShopMenu(shop, player);

        ctx = new MenuContext(controller, player, menu);
        ctx.open();
    }

    // -------------------------------------------------------------------------
    // Left click Tests
    // -------------------------------------------------------------------------

    @Test
    void LeftClickShopItemSlotBuysOneAndPlacesOnCursorStack() {

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(1, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(90, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void LeftClickShopItemWithMatchingCursorStackBuysOneAndAddsToStack() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(11, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(90, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemSlotBuysStackAndPlacesOnCursorStack() {

        player.getAccount(economy.currencyCreditsId).setBalance(700);
        assertEquals(700, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(64, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemWithMatchingCursorStackBuysUntillCursorStackIsFull() {
        player.setCursorStack(new ItemStack(matchingItem, 60));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(60, player.getCursorStack().getCount());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(64, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void shiftLeftClickShopItemBuysMaxAffordableAndPlacesOnCursor() {

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(10, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void shiftLeftClickShopItemBuysMaxAffordableAndIncrementsCursorStack() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(20, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void LeftClickShopItemWithNonMatchingCursorStackSellsStack() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(0);
        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertTrue(player.getCursorStack().isEmpty());
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemWithNonMatchingCursorStackSellsStack() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(0);
        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertTrue(player.getCursorStack().isEmpty());
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
    }

    // -------------------------------------------------------------------------
    // Middle click Tests
    // -------------------------------------------------------------------------

    @Test
    void middleClickShopItemSlotBuysStackAndPlacesOnCursorStack() {

        player.getAccount(economy.currencyCreditsId).setBalance(700);
        assertEquals(700, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(64, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemWithMatchingCursorStackBuysUntillCursorStackIsFull() {
        player.setCursorStack(new ItemStack(matchingItem, 60));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(60, player.getCursorStack().getCount());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(64, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemBuysMaxAffordableAndPlacesOnCursor() {

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(10, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleLeftClickShopItemBuysMaxAffordableAndIncrementsCursorStack() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(20, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemWithNonMatchingCursorStackShowsError() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(640);
        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());
        assertEquals(matchingItem, player.getCursorStack().getItem());

        assertEquals(
            "Stack does not match shop item",
            player.getReceivedMessages().getLast().getString()
        );
    }

    // -------------------------------------------------------------------------
    // Right click Tests
    // -------------------------------------------------------------------------

    @Test
    void rightClickWithoutCursorStackSellsOneFromInventory() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getInventory().count(matchingItem));
    }

    @Test
    void rightClickWithNonMatchingCursorStackTriesToSellOneFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.setCursorStack(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getCursorStack().getCount());
        assertEquals(64, player.getInventory().count(matchingItem));
    }

    @Test
    void rightClickWithMatchingCursorStackSellsOneFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.setCursorStack(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getCursorStack().getCount());
        assertEquals(64, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithoutCursorStackSellsStackFromInventory() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithNonMatchingCursorStackTriesToSellStackFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.setCursorStack(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().getCount());
        assertEquals(64, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithMatchingCursorStackSellsStackFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 64));
        player.setCursorStack(new ItemStack(matchingItem, 64));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().getCount());
        assertEquals(64, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithoutCursorStackSellsRemainingItemsFromInventory() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 10));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithNonMatchingCursorStackTriesToSellRemainingFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 10));
        player.setCursorStack(new ItemStack(matchingItem, 10));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().getCount());
        assertEquals(10, player.getInventory().count(matchingItem));
    }

    @Test
    void shiftRightClickWithMatchingCursorStackSellsRemainingItemsFromCursorStack() {

        player.getInventory().offerOrDrop(new ItemStack(matchingItem, 10));
        player.setCursorStack(new ItemStack(matchingItem, 10));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().getCount());
        assertEquals(10, player.getInventory().count(matchingItem));
    }

    // -------------------------------------------------------------------------
    // Misc Tests
    // -------------------------------------------------------------------------

    @Test
    void anyClickShopItemRefreshesBalance() {
        player.setCursorStack(new ItemStack(matchingItem, 10));

        player.getAccount(economy.currencyCreditsId).setBalance(10000);

        var slotIndex = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();

        controller.clearSlot(slotIndex);
        assertNull(controller.slots.get(slotIndex));
        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);
        assertNotNull(controller.slots.get(slotIndex));

        controller.clearSlot(slotIndex);
        assertNull(controller.slots.get(slotIndex));
        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);
        assertNotNull(controller.slots.get(slotIndex));

        controller.clearSlot(slotIndex);
        assertNull(controller.slots.get(slotIndex));
        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertNotNull(controller.slots.get(slotIndex));

        controller.clearSlot(slotIndex);
        assertNull(controller.slots.get(slotIndex));
        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertNotNull(controller.slots.get(slotIndex));

        controller.clearSlot(slotIndex);
        assertNull(controller.slots.get(slotIndex));
        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertNotNull(controller.slots.get(slotIndex));
    }

}
