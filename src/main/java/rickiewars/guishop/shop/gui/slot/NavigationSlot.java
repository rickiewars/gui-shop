package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NavigationSlot implements MenuSlot {
    private final String label;
    private final Supplier<Integer> targetPage;
    private final IPlayer player;
    private final Direction direction;

    public NavigationSlot(String label, Supplier<Integer> targetPage, IPlayer player, Direction direction) {
        this.label = label;
        this.targetPage = targetPage;
        this.player = player;
        this.direction = direction;
    }

    public ItemStack icon() {
        return createDirectionalCompass(
            player, direction == Direction.NEXT ? 90 : -90
        );
    }

    public Component name() {
        return Component.literal(label);
    }

    public List<Component> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {
        ctx.goToPage(targetPage.get());
    }

    public enum Direction {
        PREVIOUS,
        NEXT
    }

    private static ItemStack createDirectionalCompass(IPlayer player, float relativeYawDegrees) {
        // Player yaw (Minecraft uses degrees, 0 = south, increases clockwise)
        float yaw = player.getYaw();
        float targetYaw = yaw + relativeYawDegrees;

        // Convert yaw to direction vector
        double radians = Math.toRadians(targetYaw);
        double dx = -Math.sin(radians);
        double dz =  Math.cos(radians);

        // Place fake lodestone far away so wobble is minimal
        int distance = 1000;

        BlockPos targetPos = player.getBlockPos().offset(
            (int) (dx * distance),
            0,
            (int) (dz * distance)
        );

        ItemStack stack = new ItemStack(Items.COMPASS);

        // Lodestone tracker component (1.21+)
        stack.set(DataComponents.LODESTONE_TRACKER,
            new LodestoneTracker(
                Optional.of(new GlobalPos(player.getWorldId(), targetPos)),
                false
            )
        );

        return stack;
    }
}