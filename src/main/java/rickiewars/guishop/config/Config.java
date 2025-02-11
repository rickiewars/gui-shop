package rickiewars.guishop.config;

import net.minecraft.util.Identifier;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyAccount;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;
import rickiewars.guishop.shop.Shop;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * An interface representing the configuration structure
 */
public class Config {
    public static final String FILE_NAME = "guishop.json";
    /**
     * Configure default economy or economies provided by other mods
     */
    public EconomyProviders economyProviders;

    public LinkedList<Shop> shops;

    public Config(
            LinkedList<Shop> shops,
            EconomyProviders economyProviders
    ) {
        this.shops = shops;
        this.economyProviders = economyProviders;
    }
    public Config(LinkedList<Shop> shops){
        this(shops, new EconomyProviders());
    }
    public Config(){
        this(new LinkedList<>());
    }

    public boolean economyProvidersConfigured() {
        return economyProviders != null && !economyProviders.isEmpty();
    }

    public void configureDefaultEconomyProvider() {
        if (economyProviders == null) economyProviders = new EconomyProviders();
        economyProviders.put(
            GuiShopEconomyCurrency.DEFAULT_ID,
            new LinkedList<>() {{
                add(GuiShopEconomyAccount.DEFAULT_ID.getPath());
            }}
        );
    }

    public static class EconomyProviders extends HashMap<Identifier, List<String>> {
        public EconomyProviders() {
            super();
        }

        public Identifier getFirstCurrency() {
            return this.entrySet().iterator().next().getKey();
        }
    }
}
