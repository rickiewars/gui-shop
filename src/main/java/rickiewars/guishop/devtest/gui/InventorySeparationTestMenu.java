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
import rickiewars.guishop.devtest.gui.slot.GiveMarkerItemSlot;
import rickiewars.guishop.devtest.gui.slot.StaticInfoSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Confirms the real (vanilla) inventory rows at the bottom of the screen keep behaving normally,
 * and don't interact with this GUI's custom top slots, while the menu stays open.
 */
public class InventorySeparationTestMenu implements Menu {
    private final IPlayer player;

    private static final MenuConfig<SlotType> config = new MenuConfig<>(MenuType.GENERIC_9x3, Map.of(
        4, SlotType.INSTRUCTIONS,
        13, SlotType.GIVE_MARKER,
        22, SlotType.BACK
    ));

    public enum SlotType {
        INSTRUCTIONS,
        GIVE_MARKER,
        BACK
    }

    public InventorySeparationTestMenu(IPlayer player) {
        this.player = player;
    }

    @Override
    public Component title() {
        return Component.literal("Dev Test: Inventory separation");
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
                Component.literal("1. Click the paper slot to get a \"" + GiveMarkerItemSlot.MARKER_NAME + "\"."),
                Component.literal("2. Shift-click it in your real inventory rows below,"),
                Component.literal("   while this GUI stays open."),
                Component.literal("It should behave like any normal item, and nothing"),
                Component.literal("in this GUI's top slots should react to it.")
            )
        ));
        result.put(13, new GiveMarkerItemSlot());
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
