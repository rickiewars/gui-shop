package rickiewars.guishop.shop.gui;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopMenuTest extends MinecraftTest {

    @Test
    void pageCountRoundsUpCorrectly() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(2 * 45 + 1);

        ShopMenu menu = new ShopMenu(shop, player);

        assertEquals(3, menu.getPageCount());
    }

    @Test
    void firstPageContainsExpectedNumberOfSlots() {
        Shop shop = TestUtils.testShop(2 * 45);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player);

        assertEquals(45, menu.getPageContent(1).size());
    }

    @Test
    void secondPageSkipsFirstPageItems() {
        Shop shop = TestUtils.testShop(2 * 45);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player);
        List<MenuSlot> slots = menu.getPageContent(2);

        assertEquals(Text.of("Item 46"), slots.getFirst().name());
        assertEquals(Text.of("Item 90"), slots.getLast().name());
    }

    @Test
    void lastPageDoesNotExceedItemCount() {
        Shop shop = TestUtils.testShop(45 + 1);
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        ShopMenu menu = new ShopMenu(shop, player);
        List<MenuSlot> slots = menu.getPageContent(2);

        assertEquals(1, slots.size());
    }
}
