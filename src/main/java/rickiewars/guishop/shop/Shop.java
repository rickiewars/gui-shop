package rickiewars.guishop.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.economy.EconomyUtils;
import rickiewars.guishop.util.CommonMethods;

import java.util.LinkedList;
import java.util.List;

/**
 * A shop which holds a list of items
 */
public class Shop {
    private final String name;
    private final List<ShopItem> items;
    @Nullable
    private final Identifier defaultCurrencyId;
    private final Identifier icon;

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

    public Identifier getCurrencyId(ShopItem item) {
        if (item.hasCurrency()) {
            return item.currencyId();
        }
        return getDefaultCurrencyId();
    }

    public Identifier getDefaultCurrencyId() {
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

    public Shop(String name, List<ShopItem> items, @Nullable Identifier defaultCurrencyId) {
        this(name, items, defaultCurrencyId, null);
    }

    public Shop(String name, List<ShopItem> items, @Nullable Identifier defaultCurrencyId, @Nullable Identifier icon) {
        this.name = name;
        this.items = items;
        this.defaultCurrencyId = defaultCurrencyId;
        this.icon = icon != null ? icon : Identifier.ofVanilla("chest");
    }

    public Identifier iconId() {
        return icon;
    }

    public ItemStack getIcon() {
        return new ItemStack(CommonMethods.getItem(
            icon.toString(),
            Items.CHEST
        ));
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
