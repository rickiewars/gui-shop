package rickiewars.guishop.api.gui;

import net.minecraft.text.Text;

public interface MenuController {
    boolean open();
    void close();
    void setTitle(Text title);
    void setSlot(MenuContext context, int index, MenuSlot slot);
    void clearSlot(int inxex);
}
