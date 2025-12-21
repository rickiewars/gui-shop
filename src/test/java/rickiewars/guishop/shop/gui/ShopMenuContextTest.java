package rickiewars.guishop.shop.gui;

import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.slot.*;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShopMenuContextTest extends EconomyTest {

    @Test
    void ShopMenuPaginationDoesNotOverflow() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        ctx.goToPage(5);
        assertEquals(3, ctx.page());

        ctx.goToPage(0);
        assertEquals(1, ctx.page());
    }

    @Test
    void ShopMenuRendersSecondPageShopItems() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        ctx.goToPage(2);
        assertEquals(2, ctx.page());

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertEquals(Text.of("Item 6"), controller.slots.get(0).name());
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(4));
        assertEquals(Text.of("Item 10"), controller.slots.get(4).name());
    }

    @Test
    void ShopMenuRendersStaticSlots() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();
        ctx.goToPage(2);

        assertInstanceOf(BalanceSlot.class, controller.slots.get(menuConfig.balanceSlot()));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(menuConfig.previousPageSlot()));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(menuConfig.pageIndicatorSlot()));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(menuConfig.nextPageSlot()));
        assertInstanceOf(ExitSlot.class, controller.slots.get(menuConfig.exitSlot()));
    }

    @Test
    void FirstPageDoesNotRenderPreviousPageSlotButDoesRenderNextPageSlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        assertNull(controller.slots.get(menuConfig.previousPageSlot()));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(menuConfig.pageIndicatorSlot()));
        assertInstanceOf(NavigationSlot.class, controller.slots.get(menuConfig.nextPageSlot()));
    }

    @Test
    void LastPageDoesNotRenderNextPageSlotButDoesRenderPreviousPageSlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();
        ctx.goToPage(3);

        assertInstanceOf(NavigationSlot.class, controller.slots.get(menuConfig.previousPageSlot()));
        assertInstanceOf(PageIndicatorSlot.class, controller.slots.get(menuConfig.pageIndicatorSlot()));
        assertNull(controller.slots.get(menuConfig.nextPageSlot()));
    }

    @Test
    void refreshRerendersSamePage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 5));

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();
        ctx.goToPage(2);

        controller.slots.clear();
        ctx.refresh();

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertEquals(Text.of("Item 6"), controller.slots.get(0).name());
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(4));
        assertEquals(Text.of("Item 10"), controller.slots.get(4).name());
    }

    @Test
    void onLastPageRemainingSlotsAreRenderedAsEmptySlot() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 7);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 5));

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();
        ctx.goToPage(2);

        controller.slots.clear();
        ctx.refresh();

        assertInstanceOf(ShopItemSlot.class, controller.slots.get(0));
        assertInstanceOf(ShopItemSlot.class, controller.slots.get(1));
        assertInstanceOf(EmptySlot.class, controller.slots.get(2));
        assertInstanceOf(EmptySlot.class, controller.slots.get(3));
        assertInstanceOf(EmptySlot.class, controller.slots.get(4));
        assertEquals(Items.AIR, controller.slots.get(4).icon().getItem());
    }
}