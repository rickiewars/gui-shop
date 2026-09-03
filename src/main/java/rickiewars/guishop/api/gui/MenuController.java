package rickiewars.guishop.api.gui;

import net.minecraft.network.chat.Component;

public interface MenuController {
    boolean open(Menu menu);
    void close();
    void setTitle(Component title);
    void setSlot(int index, MenuSlot slot);
    void clearSlot(int index);
}
