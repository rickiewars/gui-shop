package rickiewars.guishop.api.gui.impl;

import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.MenuSlot;

import java.util.HashMap;
import java.util.Map;

public class TestMenuController implements MenuController {

    public boolean opened = false;

    public Text title;
    public final Map<Integer, MenuSlot> slots = new HashMap<>();

    @Override
    public boolean open() {
        opened = true;
        return true;
    }

    @Override
    public void close() {
        opened = false;
    }

    @Override
    public void setTitle(Text title) {
        this.title = title;
    }

    @Override
    public void setSlot(MenuContext context, int index, MenuSlot slot) {
        slots.put(index, slot);
    }

    @Override
    public void clearSlot(int index) {
        slots.remove(index);
    }
}
