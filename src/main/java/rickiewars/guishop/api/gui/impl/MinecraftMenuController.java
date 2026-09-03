package rickiewars.guishop.api.gui.impl;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;

public class MinecraftMenuController implements MenuController {
    SimpleGui gui;
    MenuContext context;
    final ServerPlayer player;

    public MinecraftMenuController(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public boolean open(Menu menu) {
        if (gui != null) {
            gui.close();
        }

        this.gui = new SimpleGui(
            menu.config().handlerType(),
            player,
            false
        );
        this.context = new MenuContext(this, new MinecraftPlayer(player), menu);
        return gui.open();
    }

    @Override
    public void setSlot(int index, MenuSlot slot) {
        gui.setSlot(index, buildElement(slot));
    }

    @Override
    public void clearSlot(int index) {
        gui.clearSlot(index);
    }

    @Override
    public void close() {
        gui.close();
    }

    @Override
    public void setTitle(Component title) {
        gui.setTitle(title);
    }

    private GuiElementBuilder buildElement(MenuSlot slot) {
        GuiElementBuilder b = GuiElementBuilder.from(slot.icon());

        Component name = slot.name();
        if (name != null) b.setName(name);

        var lore = slot.lore();
        if (lore != null && !lore.isEmpty()) b.setLore(lore);

        b.setCallback((index, click, action, gui) ->
            slot.onClick(context, click)
        );

        return b;
    }
}
