package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.gui.impl.TestMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.TestUtils;

import java.util.List;
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
    void BalanceSlotRendersDefaultCurrencyBalance() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        Shop shop = TestUtils.testShop(economy.currencyCreditsId, 15);
        player.addDefaultAccount(shop.getDefaultCurrencyId());

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);

        var balanceSlot = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();

        assertEquals(
            "Your balance",
            controller.slots.get(balanceSlot).name().getString()
        );

        assertEquals(1,controller.slots.get(balanceSlot).lore().size());
        assertEquals(
            "Credits: $0.00",
            controller.slots.get(balanceSlot).lore().getFirst().getString()
        );

        player.getAccount(economy.currencyCreditsId).setBalance(525);
        assertEquals(1,controller.slots.get(balanceSlot).lore().size());
        assertEquals(
            "Credits: $5.25",
            controller.slots.get(balanceSlot).lore().getFirst().getString()
        );
    }

    @Test
    void BalanceSlotRendersBalancesFromOtherCurrenciesUsedInShop() {
        TestPlayer player = new TestPlayer(UUID.randomUUID());

        var currencyId1 = economy.currencyCreditsId;
        var currencyId2 = economy.currencyCoinsId;

        var shop = new Shop("test_shop", "Test shop", List.of(
            new ShopItem(
                "Item 1",
                new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
                10,
                10,
                currencyId1,
                List.of()
            ),
            new ShopItem(
                "Item 2",
                new MinecraftItemStack(new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:stone")))),
                10,
                10,
                currencyId2,
                List.of()
            )
        ), currencyId2);

        player.addDefaultAccount(currencyId1);
        player.addDefaultAccount(currencyId2);

        TestMenuController controller = new TestMenuController(player);
        ShopMenu menu = new ShopMenu(shop, player);
        controller.open(menu);

        var balanceSlot = menu.config().indexOf(ShopMenu.SlotType.BALANCE).orElseThrow();

        player.getAccount(currencyId1).setBalance(525);
        player.getAccount(currencyId2).setBalance(123);

        assertEquals(2,controller.slots.get(balanceSlot).lore().size());

        assertEquals(
            "Coins: 123 Coins",
            controller.slots.get(balanceSlot).lore().getFirst().getString()
        );
        assertEquals(
            "Credits: $5.25",
            controller.slots.get(balanceSlot).lore().getLast().getString()
        );
    }
}
