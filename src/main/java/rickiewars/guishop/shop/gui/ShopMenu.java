package rickiewars.guishop.shop.gui;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.shop.gui.slot.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenu implements Menu {
    private final Shop shop;
    private final IPlayer player;

    private static final MenuConfig<SlotType> config = buildConfig();

    public enum SlotType {
        BALANCE,
        PREVIOUS_PAGE,
        NEXT_PAGE,
        PAGE_INDICATOR,
        EXIT,
        EMPTY
    }

    public ShopMenu(Shop shop, IPlayer player) {
        this.shop = shop;
        this.player = player;
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
    public List<MenuSlot> getPageContent(int page) {
        int pageSize = config.availableSlots();
        int start = (page - 1) * pageSize;

        List<MenuSlot> result = new ArrayList<>();

        for (int i = 0; i < pageSize; i++) {
            int shopIndex = start + i;
            if (shopIndex >= shop.getItems().size()) break;
            result.add(shopItemSlot(shop.getItems().get(shopIndex)));
        }

        return result;
    }

    @Override
    public Map<Integer, MenuSlot> getFixedSlots(int page) {
        int pageSize = config.totalSlots();
        Map<Integer, MenuSlot> result = new HashMap<>();

        for (int i = 0; i < pageSize; i++) {
            var slotType = config.fixedSlots().get(i);
            if (slotType == null) continue;
            result.put(i, switch (slotType) {
                case BALANCE -> balanceSlot(this.player);
                case PREVIOUS_PAGE -> page <= 1
                    ? emptySlot()
                    : previousPageSlot(page);
                case PAGE_INDICATOR -> pageIndicatorSlot(page);
                case NEXT_PAGE -> page >= getPageCount()
                    ? emptySlot()
                    : nextPageSlot(page);
                case EXIT -> exitSlot();
                case EMPTY -> emptySlot();
            });
        }

        return result;
    }

    @Override
    public MenuConfig<SlotType> config() {
        return config;
    }

    @Override
    public MenuSlot emptySlot() {
        return new EmptySlot(shop);
    }

    private MenuSlot balanceSlot(IPlayer player) {
        return new BalanceSlot(player, shop.getDefaultCurrencyId());
    }

    private MenuSlot previousPageSlot(int page) {
        return new NavigationSlot("Previous page", () -> page - 1, this.player, NavigationSlot.Direction.PREVIOUS);
    }

    private MenuSlot nextPageSlot(int page) {
        return new NavigationSlot("Next page", () -> page + 1, this.player, NavigationSlot.Direction.NEXT);
    }

    private MenuSlot pageIndicatorSlot(int page) {
        return new PageIndicatorSlot(page, getPageCount());
    }

    private MenuSlot exitSlot() {
        return new ExitSlot();
    }

    private MenuSlot shopItemSlot(ShopItem item) {
        return new ShopItemSlot(shop, item);
    }

    private static MenuConfig<SlotType> buildConfig() {
        int lastDynamicSlot = 5 * 9 - 1;
        return new MenuConfig<>(ScreenHandlerType.GENERIC_9X6, Map.of(
            lastDynamicSlot + 1, SlotType.BALANCE,
            lastDynamicSlot + 2, SlotType.EMPTY,
            lastDynamicSlot + 3, SlotType.EMPTY,
            lastDynamicSlot + 4, SlotType.PREVIOUS_PAGE,
            lastDynamicSlot + 5, SlotType.PAGE_INDICATOR,
            lastDynamicSlot + 6, SlotType.NEXT_PAGE,
            lastDynamicSlot + 7, SlotType.EMPTY,
            lastDynamicSlot + 8, SlotType.EMPTY,
            lastDynamicSlot + 9, SlotType.EXIT
        ));
    }
}