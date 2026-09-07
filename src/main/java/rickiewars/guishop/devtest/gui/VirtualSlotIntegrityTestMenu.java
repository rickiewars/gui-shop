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
import rickiewars.guishop.devtest.gui.slot.StaticInfoSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Two identical, inert decorative slots (no-op {@code onClick}, exactly like the production
 * {@code BalanceSlot}/{@code PageIndicatorSlot}) -- two rather than one so click-dragging has
 * somewhere to drag *to*. Confirms sgui's virtual slots can't be picked up, dropped, hotbar-swapped,
 * offhand-swapped, double-click-collected, or dragged out, for every click type it recognizes --
 * not just the ones the mod's code happens to branch on.
 */
public class VirtualSlotIntegrityTestMenu implements Menu {
    private final IPlayer player;

    private static final MenuConfig<SlotType> config = new MenuConfig<>(MenuType.GENERIC_9x3, Map.of(
        4, SlotType.INSTRUCTIONS,
        11, SlotType.SUBJECT_A,
        15, SlotType.SUBJECT_B,
        22, SlotType.BACK
    ));

    public enum SlotType {
        INSTRUCTIONS,
        SUBJECT_A,
        SUBJECT_B,
        BACK
    }

    public VirtualSlotIntegrityTestMenu(IPlayer player) {
        this.player = player;
    }

    @Override
    public Component title() {
        return Component.literal("Dev Test: Virtual slot integrity");
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
                Component.literal("Try on the two barrier slots below:"),
                Component.literal("left/right/shift click, middle click,"),
                Component.literal("number keys 1-9, drop, ctrl+drop,"),
                Component.literal("offhand swap, double click, and"),
                Component.literal("click-dragging across both slots."),
                Component.literal("Neither slot's item should ever change,"),
                Component.literal("and nothing should appear in your real"),
                Component.literal("inventory/hotbar/offhand or on the ground.")
            )
        ));
        StaticInfoSlot subject = new StaticInfoSlot(
            new ItemStack(Items.BARRIER),
            "Don't pick me up",
            List.of(Component.literal("This must be un-pickupable no matter what you click."))
        );
        result.put(11, subject);
        result.put(15, subject);
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
