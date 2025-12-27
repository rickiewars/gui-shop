package rickiewars.guishop.shop.gui;

import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.slot.*;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MenuContextTest extends EconomyTest {

    @Test
    void ShopMenuPaginationDoesNotOverflow() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(5);
        assertEquals(3, ctx.page());

        ctx.goToPage(0);
        assertEquals(1, ctx.page());
    }

    @Test
    void ShopMenuRendersSecondPageShopItems() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(2);
        assertEquals(2, ctx.page());

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertEquals(Text.of("Item 46"), controller.slots.get(0).name());
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(44));
        assertEquals(Text.of("Item 90"), controller.slots.get(44).name());
    }

    @Test
    void ShopMenuRendersStaticSlots() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(2);

        var balanceIndex = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();
        var previousPageIndex = menu.config().indexOf(ShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();
        var pageIndicatorIndex = menu.config().indexOf(ShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();
        var nextPageIndex = menu.config().indexOf(ShopMenu.SlotType.NEXT_PAGE).orElseThrow();
        var exitIndex = menu.config().indexOf(ShopMenu.SlotType.EXIT).orElseThrow();

        assertInstanceOf(BalanceSlot.class, controller.slots.get(balanceIndex));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(previousPageIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorIndex));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(nextPageIndex));
        assertInstanceOf(ExitSlot.class, controller.slots.get(exitIndex));
    }

    @Test
    void FirstPageDoesNotRenderPreviousPageSlotButDoesRenderNextPageSlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 2 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);

        var previousPageIndex = menu.config().indexOf(ShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();
        var pageIndicatorIndex = menu.config().indexOf(ShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();
        var nextPageIndex = menu.config().indexOf(ShopMenu.SlotType.NEXT_PAGE).orElseThrow();

        assertInstanceOf(EmptySlot.class, controller.slots.get(previousPageIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorIndex));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(nextPageIndex));
    }

    @Test
    void LastPageDoesNotRenderNextPageSlotButDoesRenderPreviousPageSlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(3);

        var previousPageIndex = menu.config().indexOf(ShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();
        var pageIndicatorIndex = menu.config().indexOf(ShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();
        var nextPageIndex = menu.config().indexOf(ShopMenu.SlotType.NEXT_PAGE).orElseThrow();

        assertInstanceOf(NavigationSlot.class, controller.slots.get(previousPageIndex));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(pageIndicatorIndex));
        assertInstanceOf(EmptySlot.class, controller.slots.get(nextPageIndex));
    }

    @Test
    void refreshRerendersSamePage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(2);

        controller.slots.clear();
        ctx.refresh();

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertEquals(Text.of("Item 46"), controller.slots.get(0).name());
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(44));
        assertEquals(Text.of("Item 90"), controller.slots.get(44).name());
    }

    @Test
    void onLastPageRemainingSlotsAreRenderedAsEmptySlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 45 + 2);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(2);

        controller.slots.clear();
        ctx.refresh();

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(1));
        assertInstanceOf(EmptySlot.class, controller.slots.get(2));
        assertInstanceOf(EmptySlot.class, controller.slots.get(44));
        assertEquals(Items.AIR, controller.slots.get(44).icon().getItem());
    }
}