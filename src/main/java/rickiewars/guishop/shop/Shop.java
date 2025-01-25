package rickiewars.guishop.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A shop which holds a list of items
 */
public class Shop {
    private final String name;
    private final List<ShopItem> items;
    @Nullable
    private final String defaultCurrencyId;

    public String getName() {
        return name;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public ShopItem findItem(ItemStack items) {
        String itemId = Registries.ITEM.getId(items.getItem()).toString();
        for (ShopItem shopItem : this.items) {
            if (shopItem.itemId().equals(itemId) && shopItem.matches(items)) {
                return shopItem;
            }
        }
        return null;
    }

    public String getCurrencyId(ShopItem item) {
        if (item.hasCurrency()) {
            return item.currencyId();
        }
        return getDefaultCurrencyId();
    }

    public String getDefaultCurrencyId() {
        if (hasDefaultCurrency()) {
            return defaultCurrencyId;
        }
        return EconomyUtils.getFirstCurrencyId();
    }

    public boolean hasDefaultCurrency() {
        return defaultCurrencyId != null;
    }

    public Shop(String name) {
        this(name, new LinkedList<>(), null);
    }

    public Shop(String name, List<ShopItem> items, @Nullable String defaultCurrencyId) {
        this.name = name;
        this.items = items;
        this.defaultCurrencyId = defaultCurrencyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shop shop = (Shop) o;

        return name.equals(shop.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
