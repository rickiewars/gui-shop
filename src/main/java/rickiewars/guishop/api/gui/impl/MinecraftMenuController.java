package rickiewars.guishop.api.gui.impl;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.MenuSlot;

public class MinecraftMenuController extends SimpleGui implements MenuController {
    public MinecraftMenuController(Menu menu, ServerPlayerEntity player, boolean manipulatePlayerSlots) {
        super(menu.config().handlerType(), player, manipulatePlayerSlots);
    }

    @Override
    public void setSlot(MenuContext context, int index, MenuSlot slot) {
        super.setSlot(index, buildElement(slot, context));
    }

    @Override
    public void close() {
        super.close();
    }

    private GuiElementBuilder buildElement(MenuSlot slot, MenuContext context) {
        GuiElementBuilder b = GuiElementBuilder.from(slot.icon());

        Text name = slot.name();
        if (name != null) b.setName(name);

        var lore = slot.lore();
        if (lore != null && !lore.isEmpty()) b.setLore(lore);

        b.setCallback((index, click, action, gui) ->
            slot.onClick(context, click)
        );

        return b;
    }
}
