package rickiewars.guishop.devtest.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.devtest.gui.slot.DevTestCaseSlot;
import rickiewars.guishop.shop.gui.slot.ExitSlot;

import java.util.List;
import java.util.Map;

/**
 * Root menu for the dev-only in-game sgui manual test tool. Each entry opens a dedicated test
 * screen that exercises exactly one sgui behavior the mod's real GUI code depends on -- see
 * {@code ShopItemSlot}/{@code EmptySlot}/{@code BalanceSlot} for what those are.
 */
public class DevTestMenu implements Menu {
    private final IPlayer player;

    private static final MenuConfig<SlotType> config = new MenuConfig<>(MenuType.GENERIC_9x1, Map.of(8, SlotType.EXIT));

    public enum SlotType {
        EXIT
    }

    public DevTestMenu(IPlayer player) {
        this.player = player;
    }

    @Override
    public Component title() {
        return Component.literal("SGUI Dev Tests");
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public List<MenuSlot> getPageContent(int page) {
        return List.of(
            new DevTestCaseSlot(
                new ItemStack(Items.NETHER_STAR),
                "Cursor interactions",
                List.of(Component.literal("Buy/sell cursor round-trip and click-type detection")),
                () -> new CursorInteractionsTestMenu(player)
            ),
            new DevTestCaseSlot(
                new ItemStack(Items.BARRIER),
                "Virtual slot integrity",
                List.of(Component.literal("Confirm decorative slots can't be picked up, dropped or duplicated")),
                () -> new VirtualSlotIntegrityTestMenu(player)
            ),
            new DevTestCaseSlot(
                new ItemStack(Items.PAPER),
                "Inventory separation",
                List.of(Component.literal("Confirm your real inventory still behaves normally while this GUI is open")),
                () -> new InventorySeparationTestMenu(player)
            )
        );
    }

    @Override
    public Map<Integer, MenuSlot> getFixedSlots(int page) {
        return Map.of(8, new ExitSlot());
    }

    @Override
    public MenuConfig<SlotType> config() {
        return config;
    }

    @Override
    public MenuSlot emptySlot() {
        return null;
    }
}
