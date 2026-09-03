package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
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
            BuiltInRegistries.ITEM.getKey(matchingItem).toString(),
            BuiltInRegistries.ITEM.getKey(Items.COBBLESTONE).toString()
        ), economy.currencyCreditsId,5);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        controller = new TestMenuController(player);
        menu = new ShopMenu(shop, player);
        controller.open(menu);
        ctx = controller.context;
    }

    private static ResourceId idOf(Item item) {
        return new MinecraftItemStack(new ItemStack(item)).itemId();
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
        assertEquals(1, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(90, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void LeftClickShopItemWithMatchingCursorStackBuysOneAndAddsToStack() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(11, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(90, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemSlotBuysStackAndPlacesOnCursorStack() {

        player.getAccount(economy.currencyCreditsId).setBalance(700);
        assertEquals(700, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(64, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemWithMatchingCursorStackBuysUntillCursorStackIsFull() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 60)));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(60, player.getCursorStack().count());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(64, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void shiftLeftClickShopItemBuysMaxAffordableAndPlacesOnCursor() {

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(10, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void shiftLeftClickShopItemBuysMaxAffordableAndIncrementsCursorStack() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT_SHIFT);
        assertEquals(20, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void LeftClickShopItemWithNonMatchingCursorStackSellsStack() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(0);
        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertTrue(player.getCursorStack().isEmpty());
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void ShiftLeftClickShopItemWithNonMatchingCursorStackSellsStack() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(0);
        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

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
        assertEquals(64, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemWithMatchingCursorStackBuysUntillCursorStackIsFull() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 60)));

        player.getAccount(economy.currencyCreditsId).setBalance(100);
        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(60, player.getCursorStack().count());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(64, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(60, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemBuysMaxAffordableAndPlacesOnCursor() {

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(10, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleLeftClickShopItemBuysMaxAffordableAndIncrementsCursorStack() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(105);
        assertEquals(105, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(20, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());
        assertEquals(5, player.getAccount(economy.currencyCreditsId).balance());
    }

    @Test
    void middleClickShopItemWithNonMatchingCursorStackShowsError() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

        player.getAccount(economy.currencyCreditsId).setBalance(640);
        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_MIDDLE);
        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());
        assertEquals(matchingItem, ((MinecraftItemStack) player.getCursorStack()).stack().getItem());

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

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void rightClickWithNonMatchingCursorStackTriesToSellOneFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getCursorStack().count());
        assertEquals(64, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void rightClickWithMatchingCursorStackSellsOneFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(63, player.getCursorStack().count());
        assertEquals(64, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithoutCursorStackSellsStackFromInventory() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithNonMatchingCursorStackTriesToSellStackFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().count());
        assertEquals(64, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithMatchingCursorStackSellsStackFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 64)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(640, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().count());
        assertEquals(64, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithoutCursorStackSellsRemainingItemsFromInventory() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 10)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithNonMatchingCursorStackTriesToSellRemainingFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 10)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(nonMatchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().count());
        assertEquals(10, player.getInventory().count(idOf(matchingItem)));
    }

    @Test
    void shiftRightClickWithMatchingCursorStackSellsRemainingItemsFromCursorStack() {

        player.getInventory().offerOrDrop(new MinecraftItemStack(new ItemStack(matchingItem, 10)));
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));
        player.getAccount(economy.currencyCreditsId).setBalance(0);

        controller.slots.get(matchingItemIndex).onClick(ctx, ClickType.MOUSE_RIGHT_SHIFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(0, player.getCursorStack().count());
        assertEquals(10, player.getInventory().count(idOf(matchingItem)));
    }

    // -------------------------------------------------------------------------
    // Misc Tests
    // -------------------------------------------------------------------------

    @Test
    void anyClickShopItemRefreshesBalance() {
        player.setCursorStack(new MinecraftItemStack(new ItemStack(matchingItem, 10)));

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
