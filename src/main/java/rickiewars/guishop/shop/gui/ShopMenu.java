package rickiewars.guishop.shop.gui;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuPage;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;

public class ShopMenu implements Menu {
    private final Shop shop;
    private final IPlayer player;
    private final MenuConfig config;

    public ShopMenu(Shop shop, IPlayer player, MenuConfig config) {
        this.shop = shop;
        this.player = player;
        this.config = config;
    }

    @Override
    public Text title() {
        EconomyAccount account = player.getAccount(shop.getDefaultCurrencyId());
        String balance = account.formattedBalance().getLiteralString();
        return Text.of(shop.getName() + " (Balance: " + balance + ")");
    }

    @Override
    public int getPageCount() {
        return (int) Math.ceil(shop.getItems().size() / (double) config.availableSlots());
    }

    @Override
    public MenuPage getPage(int page) {
        return new ShopMenuPage(shop, player, page, getPageCount(), config);
    }

    @Override
    public MenuConfig config() {
        return config;
    }
}