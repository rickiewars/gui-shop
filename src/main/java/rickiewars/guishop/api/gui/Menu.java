package rickiewars.guishop.api.gui;

import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public interface Menu {
    Text title();
    List<MenuSlot> getPageContent(int page);
    int getPageCount();

    Map<Integer, MenuSlot> getFixedSlots(int page);

    MenuConfig<?> config();
    MenuSlot emptySlot();
}
