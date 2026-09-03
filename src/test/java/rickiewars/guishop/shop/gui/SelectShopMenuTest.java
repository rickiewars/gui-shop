package rickiewars.guishop.shop.gui;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.slot.ExitSlot;
import rickiewars.guishop.shop.gui.slot.NavigationSlot;
import rickiewars.guishop.shop.gui.slot.PageIndicatorSlot;
import rickiewars.guishop.shop.gui.slot.ShopEntrySlot;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SelectShopMenuTest extends EconomyTest {

    @Test
    void ShopEntrySlotOpensShopMenu() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop1 = TestUtils.testShop("shop 1", economy.currencyCreditsId, 10);
        Shop shop2 = TestUtils.testShop("shop 2", economy.currencyCreditsId, 20);

        player.addDefaultAccount(economy.currencyCreditsId);
        EconomyAccount account = player.getAccount(economy.currencyCreditsId);
        String balance = account.formattedBalance().tryCollapseToString();

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(List.of(shop1, shop2), player);
        controller.open(menu);

        assertEquals(Component.nullToEmpty("Select Shop"), controller.title);
        assertInstanceOf(ShopEntrySlot.class, controller.slots.get(0));
        controller.simulateClick(0, ClickType.MOUSE_LEFT);

        assertTrue(controller.opened);
        assertEquals(Component.nullToEmpty(shop1.getDisplayName() + " (Balance: " + balance + ")"), controller.title);
    }

    @Test
    void SecondShopEntrySlotOpensSecondShop() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop1 = TestUtils.testShop("shop 1", economy.currencyCreditsId, 10);
        Shop shop2 = TestUtils.testShop("shop 2", economy.currencyCreditsId, 20);

        player.addDefaultAccount(economy.currencyCreditsId);
        EconomyAccount account = player.getAccount(economy.currencyCreditsId);
        String balance = account.formattedBalance().tryCollapseToString();

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(List.of(shop1, shop2), player);
        controller.open(menu);

        assertEquals(Component.nullToEmpty("Select Shop"), controller.title);
        assertInstanceOf(ShopEntrySlot.class, controller.slots.get(1));
        controller.simulateClick(1, ClickType.MOUSE_LEFT);

        assertTrue(controller.opened);
        assertEquals(Component.nullToEmpty(shop2.getDisplayName() + " (Balance: " + balance + ")"), controller.title);
    }

    @Test
    void LessThanNineShopsRendersSingleLineMenuLayout() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(shops, player);
        controller.open(menu);

        assertEquals(MenuType.GENERIC_9x1, menu.config().handlerType());
        assertEquals(Component.nullToEmpty(shops.getFirst().getDisplayName()), controller.slots.get(0).name());
        assertEquals(Component.nullToEmpty(shops.getLast().getDisplayName()), controller.slots.get(7).name());
        assertInstanceOf(ExitSlot.class, controller.slots.get(8));
    }

    @Test
    void NineOrMoreShopsRendersPaginationLayout() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(shops, player);
        controller.open(menu);

        assertEquals(MenuType.GENERIC_9x2, menu.config().handlerType());
        assertEquals(Component.nullToEmpty(shops.getFirst().getDisplayName()), controller.slots.get(0).name());
        assertEquals(Component.nullToEmpty(shops.getLast().getDisplayName()), controller.slots.get(8).name());
        var exitSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.EXIT).orElseThrow();
        assertInstanceOf(ExitSlot.class, controller.slots.get(exitSlotIndex));
    }

    @Test
    void PaginationButtonsAreShownWhenRelevant() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 27; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(shops, player);
        controller.open(menu);

        var previousPageSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();
        var pageIndicatorSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();
        var nextPageSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.NEXT_PAGE).orElseThrow();

        assertNull(controller.slots.get(previousPageSlotIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorSlotIndex));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(nextPageSlotIndex));

        controller.context.goToPage(2);

        assertInstanceOf(NavigationSlot.class, controller.slots.get(previousPageSlotIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorSlotIndex));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(nextPageSlotIndex));

        controller.context.goToPage(3);

        assertInstanceOf(NavigationSlot.class, controller.slots.get(previousPageSlotIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorSlotIndex));
        assertNull(controller.slots.get(nextPageSlotIndex));
    }


    @Test
    void PaginationButtonsWorkAsExpectedShownWhenRelevant() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 27; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(shops, player);
        controller.open(menu);

        var previousPageSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();
        var pageIndicatorSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();
        var nextPageSlotIndex = menu.config().indexOf(SelectShopMenu.SlotType.NEXT_PAGE).orElseThrow();

        assertEquals(Component.nullToEmpty("Page 1 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());

        controller.simulateClick(nextPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 2 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());
        controller.simulateClick(nextPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 3 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());

        controller.simulateClick(nextPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 3 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());

        controller.simulateClick(previousPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 2 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());
        controller.simulateClick(previousPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 1 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());

        controller.simulateClick(previousPageSlotIndex, ClickType.MOUSE_LEFT);
        assertEquals(Component.nullToEmpty("Page 1 / 3"), controller.slots.get(pageIndicatorSlotIndex).name());
    }

    @Test
    void SecondPageSkipsShopsFromFirstPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 27; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        TestMenuController controller = new TestMenuController(player);
        var menu = new SelectShopMenu(shops, player);
        controller.open(menu);

        controller.context.goToPage(2);
        assertEquals(Component.nullToEmpty("shop 9"), controller.slots.get(0).name());
        assertEquals(Component.nullToEmpty("shop 17"), controller.slots.get(8).name());
    }

    @Test
    void NumberOfPagesRoundsUp() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        var menu = new SelectShopMenu(shops, player);

        menu.getPageCount();
        assertEquals(2, menu.getPageCount());
    }

    @Test
    void secondPageReturnsRemainingShops() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        List<Shop> shops = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            shops.add(TestUtils.testShop("shop " + i, economy.currencyCreditsId, 10));
        }

        player.addDefaultAccount(economy.currencyCreditsId);

        var menu = new SelectShopMenu(shops, player);

        menu.getPageCount();
        assertEquals(2, menu.getPageContent(2).size());
    }
}
