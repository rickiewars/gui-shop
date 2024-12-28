package unsafedodo.guishop.config;

import unsafedodo.guishop.shop.Shop;

/**
 * An interface representing the configuration structure
 */
public class ConfigData {
        Shop[] shops;

    public ConfigData(Shop[] shops) {
        this.shops = shops;
    }

    public ConfigData(){
        this(null);
    }

}
