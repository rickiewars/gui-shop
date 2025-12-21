package rickiewars.guishop.api.gui;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface MenuPage {
    int rows();
    List<MenuSlot> slots();
    Map<Integer, MenuSlot> fixedSlots();
    default @Nullable MenuSlot emptySlot() {
        return null;
    }
}