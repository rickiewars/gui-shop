package rickiewars.guishop.devtest.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;
import java.util.function.Supplier;

/** An entry in the dev-test root menu: click it to open the test screen it describes. */
public class DevTestCaseSlot implements MenuSlot {
    private final ItemStack icon;
    private final String label;
    private final List<Component> description;
    private final Supplier<Menu> target;

    public DevTestCaseSlot(ItemStack icon, String label, List<Component> description, Supplier<Menu> target) {
        this.icon = icon;
        this.label = label;
        this.description = description;
        this.target = target;
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }

    @Override
    public Component name() {
        return Component.literal(label);
    }

    @Override
    public List<Component> lore() {
        return description;
    }

    @Override
    public void onClick(MenuContext ctx, ClickType click) {
        ctx.open(target.get());
    }
}
