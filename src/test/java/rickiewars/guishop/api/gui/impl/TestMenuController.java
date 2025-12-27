package rickiewars.guishop.api.gui.impl;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;

import java.util.HashMap;
import java.util.Map;

public class TestMenuController implements MenuController {
    public MenuContext context;
    private final IPlayer player;

    public boolean opened = false;

    public Text title;
    public final Map<Integer, MenuSlot> slots = new HashMap<>();

    public TestMenuController(IPlayer player) {
        this.player = player;
    }

    @Override
    public boolean open(Menu menu) {
        context = new MenuContext(this, player, menu);
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
    public void setSlot(int index, MenuSlot slot) {
        slots.put(index, slot);
    }

    @Override
    public void clearSlot(int index) {
        slots.remove(index);
    }

    public void simulateClick(int index, ClickType clickType) {
        MenuSlot slot = slots.get(index);
        if (slot != null) {
            slot.onClick(context, clickType);
        }
    }
}
