package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FixedSlotTest extends EconomyTest {

    @Test
    void ExitSlotClosesMenu() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        assertTrue(controller.opened);

        var slotIndex = menu.config().indexOf(ShopMenu.SlotType.EXIT).orElseThrow();
        controller.slots.get(slotIndex).onClick(ctx, ClickType.MOUSE_LEFT);

        assertFalse(controller.opened);
    }

    @Test
    void PageIndicatorSlotShowsCorrectPageNumber() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        var slotIndex = menu.config().indexOf(ShopMenu.SlotType.PAGE_INDICATOR).orElseThrow();

        assertEquals(
            Text.literal("Page 1 / 3"),
            controller.slots.get(slotIndex).name()
        );
        ctx.goToPage(2);
        assertEquals(
            Text.literal("Page 2 / 3"),
            controller.slots.get(slotIndex).name()
        );
        ctx.goToPage(3);
        assertEquals(
            Text.literal("Page 3 / 3"),
            controller.slots.get(slotIndex).name()
        );
    }

    @Test
    void PreviousPageSlotNavigatesToPerviousPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ctx.goToPage(3);

        var slotIndex = menu.config().indexOf(ShopMenu.SlotType.PREVIOUS_PAGE).orElseThrow();

        assertEquals(3, ctx.page());

        controller.slots.get(slotIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(2, ctx.page());

        controller.slots.get(slotIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(1, ctx.page());
    }

    @Test
    void NextPageSlotNavigatesToNextPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 3 * 45);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        var slotIndex = menu.config().indexOf(ShopMenu.SlotType.NEXT_PAGE).orElseThrow();

        assertEquals(1, ctx.page());

        controller.slots.get(slotIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(2, ctx.page());

        controller.slots.get(slotIndex).onClick(ctx, ClickType.MOUSE_LEFT);
        assertEquals(3, ctx.page());
    }

    @Test
    void BalanceSlotRendersBalance() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);

        var balanceSlot = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();

        assertEquals(
            "Your balance: $0.00",
            controller.slots.get(balanceSlot).name().getString()
        );

        player.getAccount(economy.currencyCreditsId).setBalance(525);
        assertEquals(
            "Your balance: $5.25",
            controller.slots.get(balanceSlot).name().getString()
        );
    }
}
