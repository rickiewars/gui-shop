package rickiewars.guishop.api.gui;

import rickiewars.guishop.api.minecraft.IPlayer;

public interface MenuContext {
    IPlayer player();
    int page();
    void goToPage(int page);
    boolean open();
    void close();
    void refresh();
    void refreshBalance();
}