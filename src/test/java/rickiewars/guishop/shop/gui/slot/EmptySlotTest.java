package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.TestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmptySlotTest extends EconomyTest {

    @Test
    void LeftClickEmptySlotSellsCursorStack() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = BuiltInRegistries.ITEM.getValue(shopItem.itemId());
        player.setCursorStack(new MinecraftItemStack(new ItemStack(item, 10)));

        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_LEFT);

        assertEquals(100, player.getAccount(economy.currencyCreditsId).balance());
        assertTrue(player.getCursorStack().isEmpty());
    }

    @Test
    void RightClickEmptySlotSellsSingleItemFromCursorStack() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = BuiltInRegistries.ITEM.getValue(shopItem.itemId());
        player.setCursorStack(new MinecraftItemStack(new ItemStack(item, 10)));

        assertEquals(0, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(10, player.getCursorStack().count());

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_RIGHT);

        assertEquals(10, player.getAccount(economy.currencyCreditsId).balance());
        assertEquals(9, player.getCursorStack().count());
    }

    @Test
    void ActionOnEmptySlotRerendersBalance() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 1);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);
        var ctx = controller.context;

        ShopItem shopItem = shop.getItems().getFirst();
        Item item = BuiltInRegistries.ITEM.getValue(shopItem.itemId());
        player.setCursorStack(new MinecraftItemStack(new ItemStack(item, 10)));

        var balanceSlot = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();

        controller.clearSlot(balanceSlot);
        assertNull(controller.slots.get(balanceSlot));

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_RIGHT);
        assertNotNull(controller.slots.get(balanceSlot));

        controller.clearSlot(balanceSlot);
        assertNull(controller.slots.get(balanceSlot));

        controller.slots.get(1).onClick(ctx, ClickType.MOUSE_LEFT);
        assertNotNull(controller.slots.get(balanceSlot));
    }
}
