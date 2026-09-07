package rickiewars.guishop.devtest.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.devtest.gui.slot.BackSlot;
import rickiewars.guishop.devtest.gui.slot.CursorDropOffSlot;
import rickiewars.guishop.devtest.gui.slot.CursorSourceSlot;
import rickiewars.guishop.devtest.gui.slot.StaticInfoSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CursorInteractionsTestMenu implements Menu {
    private final IPlayer player;

    private static final MenuConfig<SlotType> config = new MenuConfig<>(MenuType.GENERIC_9x3, Map.of(
        4, SlotType.INSTRUCTIONS,
        11, SlotType.SOURCE,
        15, SlotType.DROPOFF,
        22, SlotType.BACK
    ));

    public enum SlotType {
        INSTRUCTIONS,
        SOURCE,
        DROPOFF,
        BACK
    }

    public CursorInteractionsTestMenu(IPlayer player) {
        this.player = player;
    }

    @Override
    public Component title() {
        return Component.literal("Dev Test: Cursor interactions");
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public List<MenuSlot> getPageContent(int page) {
        return List.of();
    }

    @Override
    public Map<Integer, MenuSlot> getFixedSlots(int page) {
        Map<Integer, MenuSlot> result = new HashMap<>();
        result.put(4, new StaticInfoSlot(
            new ItemStack(Items.BOOK),
            "Instructions",
            List.of(
                Component.literal("Left/click the star slot to grow the cursor,"),
                Component.literal("right-click it or the hopper slot to shrink it."),
                Component.literal("See each slot's own tooltip for the full matrix.")
            )
        ));
        result.put(11, new CursorSourceSlot());
        result.put(15, new CursorDropOffSlot());
        result.put(22, new BackSlot(() -> new DevTestMenu(player)));
        return result;
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
