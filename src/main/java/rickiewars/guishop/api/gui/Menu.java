package rickiewars.guishop.api.gui;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public interface Menu {
    Component title();
    List<MenuSlot> getPageContent(int page);
    int getPageCount();

    Map<Integer, MenuSlot> getFixedSlots(int page);

    MenuConfig<?> config();
    MenuSlot emptySlot();
}
