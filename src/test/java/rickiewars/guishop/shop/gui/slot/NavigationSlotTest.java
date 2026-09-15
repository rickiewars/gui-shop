package rickiewars.guishop.shop.gui.slot;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.TestPlayer;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NavigationSlotTest extends MinecraftTest {

    @Test
    void iconPlacesLodestoneEastOfPlayerFacingNorthGoingNext() {
        assertIconOffset(180, NavigationSlot.Direction.NEXT, 1000, 0);
    }

    @Test
    void iconPlacesLodestoneWestOfPlayerFacingNorthGoingPrevious() {
        assertIconOffset(180, NavigationSlot.Direction.PREVIOUS, -1000, 0);
    }

    @Test
    void iconPlacesLodestoneWestOfPlayerFacingSouthGoingNext() {
        assertIconOffset(0, NavigationSlot.Direction.NEXT, -1000, 0);
    }

    @Test
    void iconPlacesLodestoneEastOfPlayerFacingSouthGoingPrevious() {
        assertIconOffset(0, NavigationSlot.Direction.PREVIOUS, 1000, 0);
    }

    @Test
    void iconPlacesLodestoneSouthOfPlayerFacingEastGoingNext() {
        assertIconOffset(270, NavigationSlot.Direction.NEXT, 0, 1000);
    }

    @Test
    void iconPlacesLodestoneNorthOfPlayerFacingEastGoingPrevious() {
        assertIconOffset(270, NavigationSlot.Direction.PREVIOUS, 0, -1000);
    }

    @Test
    void iconPlacesLodestoneNorthOfPlayerFacingWestGoingNext() {
        assertIconOffset(90, NavigationSlot.Direction.NEXT, 0, -1000);
    }

    @Test
    void iconPlacesLodestoneSouthOfPlayerFacingWestGoingPrevious() {
        assertIconOffset(90, NavigationSlot.Direction.PREVIOUS, 0, 1000);
    }

    private void assertIconOffset(float playerYaw, NavigationSlot.Direction direction, int dx, int dz) {
        TestPlayer player = new TestPlayer(UUID.randomUUID());
        player.setYaw(playerYaw);

        NavigationSlot slot = new NavigationSlot("Label", () -> 1, player, direction);
        ItemStack compass = slot.icon();

        assertEquals(Items.COMPASS, compass.getItem());
        LodestoneTracker tracker = compass.get(DataComponents.LODESTONE_TRACKER);
        assertNotNull(tracker);
        assertFalse(tracker.tracked());
        GlobalPos expected = new GlobalPos(Level.OVERWORLD, player.getBlockPos().offset(dx, 0, dz));
        assertEquals(Optional.of(expected), tracker.target());
    }
}
