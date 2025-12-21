package rickiewars.guishop.shop.gui;

import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuPage;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.shop.gui.slot.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenuPage implements MenuPage {
    private final Shop shop;
    private final IPlayer player;
    private final int page;
    private final int maxPage;
    private final MenuConfig config;

    public ShopMenuPage(Shop shop, IPlayer player, int page, int maxPage, MenuConfig config) {
        this.shop = shop;
        this.player = player;
        this.page = page;
        this.maxPage = maxPage;
        this.config = config;
    }

    // ----------------------------
    // Required MenuPage methods
    // ----------------------------

    @Override
    public int rows() {
        return config.availableRows();
    }

    @Override
    public List<MenuSlot> slots() {
        int pageSize = config.availableSlots();
        int start = (page - 1) * pageSize;

        List<MenuSlot> result = new ArrayList<>();

        for (int i = 0; i < pageSize; i++) {
            int index = start + i;
            if (index >= shop.getItems().size()) break;
            result.add(shopItemSlot(shop.getItems().get(index)));
        }

        return result;
    }

    @Override
    public Map<Integer, MenuSlot> fixedSlots() {
        Map<Integer, MenuSlot> map = new HashMap<>();

        map.put(config.balanceSlot(), balanceSlot(player));
        map.put(config.exitSlot(), exitSlot());

        if (page > 1) {
            map.put(config.previousPageSlot(), previousPageSlot());
        }

        map.put(config.pageIndicatorSlot(), pageIndicatorSlot());

        if (page < maxPage) {
            map.put(config.nextPageSlot(), nextPageSlot());
        }

        return map;
    }

    private MenuSlot shopItemSlot(ShopItem item) {
        return new ShopItemSlot(shop, item);
    }

    private MenuSlot balanceSlot(IPlayer player) {
        return new BalanceSlot(player, shop.getDefaultCurrencyId());
    }

    private MenuSlot previousPageSlot() {
        return new NavigationSlot("Previous page", () -> page - 1, this.player, NavigationSlot.Direction.PREVIOUS);
    }

    private MenuSlot nextPageSlot() {
        return new NavigationSlot("Next page", () -> page + 1, this.player, NavigationSlot.Direction.NEXT);
    }

    private MenuSlot pageIndicatorSlot() {
        return new PageIndicatorSlot(page, maxPage);
    }

    private MenuSlot exitSlot() {
        return new ExitSlot();
    }

    @Override
    public MenuSlot emptySlot() {
        return new EmptySlot(shop);
    }
}
