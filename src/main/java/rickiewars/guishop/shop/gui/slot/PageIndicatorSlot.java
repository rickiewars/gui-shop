package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.List;

public class PageIndicatorSlot implements MenuSlot {
    private final int page;
    private final int maxPage;

    public PageIndicatorSlot(int page, int maxPage) {
        this.page = page;
        this.maxPage = maxPage;
    }

    public ItemStack icon() {
        ItemStack paper = Items.PAPER.getDefaultInstance();
        paper.setCount(page);
        return paper;
    }

    public Component name() {
        return Component.literal("Page " + page + " / " + maxPage);
    }

    public List<Component> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
