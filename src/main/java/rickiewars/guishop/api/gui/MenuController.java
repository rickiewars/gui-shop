package rickiewars.guishop.api.gui;

import net.minecraft.text.Text;

public interface MenuController {
    boolean open(Menu menu);
    void close();
    void setTitle(Text title);
    void setSlot(int index, MenuSlot slot);
    void clearSlot(int index);
}
