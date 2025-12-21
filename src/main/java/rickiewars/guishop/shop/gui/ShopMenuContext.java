package rickiewars.guishop.shop.gui;

import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.*;
import rickiewars.guishop.api.minecraft.IPlayer;

import java.util.List;
import java.util.Map;

public final class ShopMenuContext implements MenuContext {
    private final Menu menu;
    private final MenuConfig config;
    private final IPlayer iPlayer;
    private final MenuController controller;

    private int page = 1;

    public ShopMenuContext(MenuController menuController, IPlayer player, Menu menu) {
        this.controller = menuController;

        this.menu = menu;
        this.config = menu.config();
        this.iPlayer = player;

        render();
    }

    private void render() {
        controller.setTitle(Text.of(this.menu.title()));

        clearSlots();

        MenuPage page = menu.getPage(this.page);
        List<MenuSlot> slots = page.slots();
        Map<Integer, MenuSlot> fixed = page.fixedSlots();
        MenuSlot empty = page.emptySlot();

        int maxContent = config.availableSlots();

        for (int i = 0; i < maxContent; i++) {
            MenuSlot slot = i < slots.size() ? slots.get(i) : empty;
            if (slot != null) {
                controller.setSlot(this, i, slot);
            }
        }

        if (fixed != null) {
            fixed.forEach((index, slot) -> {
                if (index >= 0 && index < config.totalSlots()) {
                    controller.setSlot(this, index, slot);
                }
            });
        }
    }

    private void clearSlots() {
        for (int i = 0; i < config.totalSlots(); i++) {
            controller.clearSlot(i);
        }
    }

    @Override
    public IPlayer player() {
        return iPlayer;
    }

    @Override
    public int page() {
        return page;
    }

    @Override
    public void goToPage(int page) {
        int max = Math.max(1, menu.getPageCount());
        this.page = Math.max(1, Math.min(page, max));
        refresh();
    }

    @Override
    public boolean open() {
        return controller.open();
    }

    @Override
    public void close() {
        controller.close();
    }

    @Override
    public void refresh() {
        render();
    }

    @Override
    public void refreshBalance() {
        controller.setTitle(Text.of(this.menu.title()));
        var balanceSlot = menu.getPage(this.page).fixedSlots().get(this.config.balanceSlot());
        if (balanceSlot != null) {
            controller.setSlot(this, this.config.balanceSlot(), balanceSlot);
        }
    }
}
