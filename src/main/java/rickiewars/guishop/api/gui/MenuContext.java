package rickiewars.guishop.api.gui;

import rickiewars.guishop.api.minecraft.IPlayer;

public class MenuContext {
    protected final Menu menu;
    protected final MenuController controller;
    protected final IPlayer player;

    private int currentPage = 1;

    public MenuContext(MenuController controller, IPlayer player, Menu menu) {
        this.controller = controller;
        this.player = player;
        this.menu = menu;
        refresh();
    }

    public IPlayer player() { return player; }
    public int page() { return currentPage; }

    public void goToPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, menu.getPageCount()));
        refresh();
    }

    public boolean open(Menu menu) { return controller.open(menu); }
    public void close() { controller.close(); }

    public void refresh() {
        renderFull();
    }

    public void partialRefresh() {
        controller.setTitle(net.minecraft.text.Text.of(menu.title()));
        renderFixed();
    }

    private void renderFull() {
        clearAll(menu.config().totalSlots());
        this.partialRefresh();
        renderContent();
    }

    private void renderContent() {
        var cfg = menu.config();
        var slots = menu.getPageContent(currentPage);
        var empty = menu.emptySlot();

        int maxContent = cfg.availableSlots();
        for (int i = 0; i < maxContent; i++) {
            MenuSlot slot = i < slots.size() ? slots.get(i) : empty;
            if (slot != null) controller.setSlot(i, slot);
        }
    }

    private void renderFixed() {
        var cfg = menu.config();
        var fixed = menu.getFixedSlots(currentPage);
        if (fixed == null) return;

        fixed.forEach((index, slot) -> {
            if (index >= 0 && index < cfg.totalSlots()) {
                controller.setSlot(index, slot);
            }
        });
    }

    private void clearAll(int totalSlots) {
        for (int i = 0; i < totalSlots; i++) controller.clearSlot(i);
    }
}

