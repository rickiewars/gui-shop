package unsafedodo.guishop.config;

import unsafedodo.guishop.economy.EconomyType;
import unsafedodo.guishop.shop.Shop;

/**
 * An interface representing the configuration structure
 */
public class ConfigData {
    EconomyType economyType;
    Shop[] shops;

    public ConfigData(Shop[] shops, EconomyType economyType) {
        this.shops = shops;
        this.economyType = economyType;
    }
    public ConfigData(Shop[] shops){
        this(shops, EconomyType.firstLoaded());
    }
    public ConfigData(){
        this(null, EconomyType.firstLoaded());
    }

}
