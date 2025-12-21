package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
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
        ItemStack paper = Items.PAPER.getDefaultStack();
        paper.setCount(page);
        return paper;
    }

    public Text name() {
        return Text.literal("Page " + page + " / " + maxPage);
    }

    public List<Text> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
