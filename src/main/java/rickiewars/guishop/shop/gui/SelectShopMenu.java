package rickiewars.guishop.shop.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.slot.ExitSlot;
import rickiewars.guishop.shop.gui.slot.NavigationSlot;
import rickiewars.guishop.shop.gui.slot.PageIndicatorSlot;
import rickiewars.guishop.shop.gui.slot.ShopEntrySlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectShopMenu implements Menu {
    private final MenuConfig<SlotType> config;
    private final List<Shop> shops;
    private final IPlayer player;

    public enum SlotType {
        PREVIOUS_PAGE,
        PAGE_INDICATOR,
        NEXT_PAGE,
        EXIT,
        EMPTY
    }

    public SelectShopMenu(List<Shop> shops, IPlayer player) {
        this.shops = shops;
        this.player = player;
        this.config = buildConfig(shops.size());
    }

    @Override
    public Component title() {
        return Component.nullToEmpty("Select Shop");
    }

    @Override
    public List<MenuSlot> getPageContent(int page) {
        int pageSize = config.availableSlots();
        int start = (page - 1) * pageSize;
        List<MenuSlot> result = new ArrayList<>();

        for (int i = start; i < Math.min(start + pageSize, shops.size()); i++) {
            Shop shop = shops.get(i);
            result.add(new ShopEntrySlot(shop, player));
        }

        return result;
    }

    @Override
    public int getPageCount() {
        return (int) Math.ceil(shops.size() / (double) config.availableSlots());
    }

    @Override
    public Map<Integer, MenuSlot> getFixedSlots(int page) {
        int pageSize = config.totalSlots();
        Map<Integer, MenuSlot> result = new HashMap<>();

        for (int i = 0; i < pageSize; i++) {
            var slotType = config.fixedSlots().get(i);
            if (slotType == null) continue;
            result.put(i, switch (slotType) {
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
        return null;
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

    private MenuConfig<SlotType> buildConfig(int entryCount) {
        int lastDynamicSlot = 9 - 1;
        return entryCount > 8
            ? new MenuConfig<>(MenuType.GENERIC_9x2, Map.of(
                lastDynamicSlot + 1, SlotType.EMPTY,
                lastDynamicSlot + 2, SlotType.EMPTY,
                lastDynamicSlot + 3, SlotType.EMPTY,
                lastDynamicSlot + 4, SlotType.PREVIOUS_PAGE,
                lastDynamicSlot + 5, SlotType.PAGE_INDICATOR,
                lastDynamicSlot + 6, SlotType.NEXT_PAGE,
                lastDynamicSlot + 7, SlotType.EMPTY,
                lastDynamicSlot + 8, SlotType.EMPTY,
                lastDynamicSlot + 9, SlotType.EXIT
            ))
            : new MenuConfig<>(MenuType.GENERIC_9x1, Map.of(
                lastDynamicSlot, SlotType.EXIT
            ));
    }
}
