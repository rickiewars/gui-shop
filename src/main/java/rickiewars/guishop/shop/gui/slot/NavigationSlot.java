package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.util.CommonMethods;

import java.util.List;
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
        return CommonMethods.createDirectionalCompass(
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
}