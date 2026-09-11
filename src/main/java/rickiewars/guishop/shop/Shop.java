package rickiewars.guishop.shop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.economy.EconomyUtils;
import rickiewars.guishop.util.CommonMethods;

import java.util.LinkedList;
import java.util.List;

/**
 * A shop which holds a list of items
 */
public class Shop {
    private final String id;
    private final String displayName;
    private final List<ShopItem> items;
    @Nullable
    private final ResourceId defaultCurrencyId;
    private final ResourceId icon;
    private final SellPricing sellPricing;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public SellPricing getSellPricing() {
        return sellPricing;
    }

    public ShopItem findHighestPayingItem(IItemStack stack) {
        ShopItem best = null;
        long bestPayout = Long.MIN_VALUE;

        for (ShopItem item : items) {
            if (item.sellPrice() == -1) continue;
            if (!item.resembles(stack)) continue;

            long payout = sellPricing.adjustedPayout(item, stack);
            if (best == null || payout > bestPayout) {
                best = item;
                bestPayout = payout;
            }
        }

        return best;
    }

    public ResourceId getCurrencyId(ShopItem item) {
        if (item.hasCurrency()) {
            return item.resolvedCurrencyId();
        }
        return getDefaultCurrencyId();
    }

    public List<ResourceId> getAllCurrencyIds() {
        var currencyIds = new LinkedList<ResourceId>();
        currencyIds.add(getDefaultCurrencyId());

        for (ShopItem item : items) {
            ResourceId currencyId = getCurrencyId(item);
            if (currencyId != defaultCurrencyId && !currencyIds.contains(currencyId)) {
                currencyIds.add(currencyId);
            }
        }

        return currencyIds;
    }

    public ResourceId getDefaultCurrencyId() {
        if (hasDefaultCurrency()) {
            return defaultCurrencyId;
        }
        return EconomyUtils.getFirstCurrencyId();
    }

    public boolean hasDefaultCurrency() {
        return defaultCurrencyId != null;
    }

    public Shop(String id, String displayName) {
        this(id, displayName, new LinkedList<>(), null);
    }

    public Shop(String id, String displayName, List<ShopItem> items, @Nullable ResourceId defaultCurrencyId) {
        this(id, displayName, items, defaultCurrencyId, null, null);
    }

    public Shop(String id, String displayName, List<ShopItem> items, @Nullable ResourceId defaultCurrencyId, @Nullable ResourceId icon) {
        this(id, displayName, items, defaultCurrencyId, icon, null);
    }

    public Shop(String id, String displayName, List<ShopItem> items, @Nullable ResourceId defaultCurrencyId, @Nullable ResourceId icon, @Nullable SellPricing sellPricing) {
        this.id = id;
        this.displayName = displayName;
        this.items = items;
        this.defaultCurrencyId = defaultCurrencyId;
        this.icon = icon != null ? icon : ResourceId.ofVanilla("chest");
        this.sellPricing = sellPricing != null ? sellPricing : GUIShop.config.sellPricing;
    }

    public ResourceId iconId() {
        return icon;
    }

    public ItemStack getIcon() {
        return new ItemStack(CommonMethods.getItem(
            icon.toString(),
            Items.CHEST
        ));
    }

    /// Load-time sanity checks and logs warnings
    public void validate() {
        for (ShopItem item : items) {
            if (item.buyPrice() < -1) {
                GUIShop.LOGGER.warn("Shop '{}': item '{}' has an invalid buyPrice {} (must be -1 or >= 0)", id, item.displayName(), item.buyPrice());
            }
            if (item.sellPrice() < -1) {
                GUIShop.LOGGER.warn("Shop '{}': item '{}' has an invalid sellPrice {} (must be -1 or >= 0)", id, item.displayName(), item.sellPrice());
            }
            if (item.buyPrice() >= 0 && item.sellPrice() >= 0 && item.sellPrice() > item.buyPrice()) {
                GUIShop.LOGGER.warn("Shop '{}': item '{}' sells for more ({}) than it costs to buy ({}) -- money loop", id, item.displayName(), item.sellPrice(), item.buyPrice());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shop shop)) return false;

        return id.equals(shop.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
