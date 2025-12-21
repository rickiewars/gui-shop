package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.shop.gui.ShopMenuContext;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FixedSlotTest extends EconomyTest {

    @Test
    void ExitSlotClosesMenu() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        assertTrue(controller.opened);

        MenuSlot slot = controller.slots.get(menuConfig.exitSlot());
        slot.onClick(ctx, ClickType.MOUSE_LEFT);

        assertFalse(controller.opened);
    }

    @Test
    void PageIndicatorSlotShowsCorrectPageNumber() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        assertEquals(
            Text.literal("Page 1 / 3"),
            controller.slots.get(menuConfig.pageIndicatorSlot()).name()
        );
        ctx.goToPage(2);
        assertEquals(
            Text.literal("Page 2 / 3"),
            controller.slots.get(menuConfig.pageIndicatorSlot()).name()
        );
        ctx.goToPage(3);
        assertEquals(
            Text.literal("Page 3 / 3"),
            controller.slots.get(menuConfig.pageIndicatorSlot()).name()
        );
    }

    @Test
    void PreviousPageSlotNavigatesToPerviousPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();
        ctx.goToPage(3);

        assertEquals(3, ctx.page());

        controller.slots.get(menuConfig.previousPageSlot()).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(2, ctx.page());

        controller.slots.get(menuConfig.previousPageSlot()).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(1, ctx.page());
    }

    @Test
    void NextPageSlotNavigatesToNextPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        assertEquals(1, ctx.page());

        controller.slots.get(menuConfig.nextPageSlot()).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(2, ctx.page());

        controller.slots.get(menuConfig.nextPageSlot()).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(3, ctx.page());
    }

    @Test
    void BalanceSlotRendersBalance() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        assertEquals(
            "Your balance: $0.00",
            controller.slots.get(menuConfig.balanceSlot()).name().getString()
        );

        player.getAccount(economy.currencyCreditsId).setBalance(525);

        assertEquals(
            "Your balance: $5.25",
            controller.slots.get(menuConfig.balanceSlot()).name().getString()
        );
    }
}
