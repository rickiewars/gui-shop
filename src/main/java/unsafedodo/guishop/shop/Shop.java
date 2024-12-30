package unsafedodo.guishop.shop;

import java.util.LinkedList;
import java.util.List;

/**
 * A shop which holds a list of items
 */
public class Shop {
    private String name;
    private List<ShopItem> items;

    public String getName() {
        return name;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public ShopItem findItem(String material) {
        for (ShopItem item : items) {
            if (item.getItemMaterial().equals(material)) {
                return item;
            }
        }
        return null;
    }

    public Shop(String name) {
        this(name, new LinkedList<>());
    }

    public Shop(String name, List<ShopItem> items) {
        this.name = name;
        this.items = items;
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
