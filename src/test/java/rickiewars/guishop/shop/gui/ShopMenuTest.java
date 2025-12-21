package rickiewars.guishop.shop.gui;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.gui.MenuPage;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShopMenuTest extends MinecraftTest {

    @Test
    void pageCountRoundsUpCorrectly() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(15);

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 5));

        assertEquals(3, menu.getPageCount());
    }

    @Test
    void getPageReturnsNonNullPage() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(1);

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 9));

        assertNotNull(menu.getPage(1));
    }

    @Test
    void rowsReturnConfiguredRows() {
        Shop shop = TestUtils.testShop(1);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(3, 9));
        MenuPage page = menu.getPage(1);

        assertEquals(3, page.rows());
    }

    @Test
    void firstPageContainsExpectedNumberOfSlots() {
        Shop shop = TestUtils.testShop(20);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(3, 5));
        MenuPage page = menu.getPage(1);

        assertEquals(15, page.slots().size());
    }

    @Test
    void secondPageSkipsFirstPageItems() {
        Shop shop = TestUtils.testShop(15);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 5));
        MenuPage page = menu.getPage(2);
        List<MenuSlot> slots = page.slots();

        assertEquals(Text.of("Item 6"), slots.getFirst().name());
        assertEquals(Text.of("Item 10"), slots.getLast().name());
    }

    @Test
    void lastPageDoesNotExceedItemCount() {
        Shop shop = TestUtils.testShop(11);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player, TestUtils.menuConfig(1, 5));
        MenuPage page = menu.getPage(3);
        List<MenuSlot> slots = page.slots();

        assertEquals(1, slots.size());
    }
}
