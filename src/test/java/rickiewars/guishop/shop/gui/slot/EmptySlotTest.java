package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.shop.gui.ShopMenuContext;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmptySlotTest extends EconomyTest {

    @Test
    void LeftClickEmptySlotSellsCursorStack() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = Registries.ITEM.get(Identifier.of(shopItem.itemId()));
        player.setCursorStack(new ItemStack(item, 10));

        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_LEFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());
    }

    @Test
    void RightClickEmptySlotSellsSingleItemFromCursorStack() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = Registries.ITEM.get(Identifier.of(shopItem.itemId()));
        player.setCursorStack(new ItemStack(item, 10));

        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().getCount());

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(9, player.getCursorStack().getCount());
    }

    @Test
    void ActionOnEmptySlotRerendersBalance() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController();
        MenuConfig menuConfig = TestUtils.menuConfig(1, 5);
        ShopMenu menu = new ShopMenu(shop, player, menuConfig);

        ShopMenuContext ctx = new ShopMenuContext(controller, player, menu);
        ctx.open();

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = Registries.ITEM.get(Identifier.of(shopItem.itemId()));
        player.setCursorStack(new ItemStack(item, 10));

        controller.clearSlot(menuConfig.balanceSlot());
        assertNull(controller.slots.get(menuConfig.balanceSlot()));

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_RIGHT);
        assertNotNull(controller.slots.get(menuConfig.balanceSlot()));

        controller.clearSlot(menuConfig.balanceSlot());
        assertNull(controller.slots.get(menuConfig.balanceSlot()));

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_LEFT);
        assertNotNull(controller.slots.get(menuConfig.balanceSlot()));
    }
}
