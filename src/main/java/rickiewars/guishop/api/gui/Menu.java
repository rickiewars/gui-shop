package rickiewars.guishop.api.gui;

import net.minecraft.text.Text;

public interface Menu {
    Text title();
    MenuPage getPage(int page);
    int getPageCount();
    MenuConfig config();
}
