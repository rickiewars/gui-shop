package rickiewars.guishop.config;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest extends MinecraftTest {
    @Test
    void defaultConstructorCreatesEmptyShopList() {
        Config config = new Config();
        assertNotNull(config.shops);
        assertEquals(0, config.shops.size());
    }

    @Test
    void economyProvidersNotConfiguredByDefault() {
        Config config = new Config();
        assertFalse(config.economyProvidersConfigured());
    }

    @Test
    void configureDefaultEconomyProviderAddsDefaultCurrencyAndAccount() {
        Config config = new Config();
        config.configureDefaultEconomyProvider();

        assertTrue(config.economyProvidersConfigured());
        assertEquals(1, config.economyProviders.size());

        Identifier key = config.economyProviders.keySet().iterator().next();
        assertEquals("guishop:credit", key.toString());
        assertEquals(1, config.economyProviders.get(key).size());
        assertEquals("account", config.economyProviders.get(key).getFirst());
    }

    @Test
    void constructorWithShopListSetsFieldCorrectly() {
        LinkedList<rickiewars.guishop.shop.Shop> shops = new LinkedList<>();
        shops.add(new rickiewars.guishop.shop.Shop("TestShop"));

        Config config = new Config(shops);

        assertEquals(1, config.shops.size());
        assertEquals("TestShop", config.shops.getFirst().getName());
    }
}
