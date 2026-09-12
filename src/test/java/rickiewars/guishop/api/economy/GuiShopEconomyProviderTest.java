package rickiewars.guishop.api.economy;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftCompat;
import rickiewars.guishop.config.GuiShopConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GuiShopEconomyProviderTest extends MinecraftTest {

    private GuiShopEconomyProvider provider;
    private GameProfile profile;

    @BeforeEach
    void setup() {
        provider = GuiShopEconomyProvider.INSTANCE;
        profile = new GameProfile(UUID.randomUUID(), "TestUser");

        GuiShopConfig config = new GuiShopConfig();
        config.economyDisabled = false;

        config.economy = new GuiShopConfig.EconomyProviderDefinition();
        config.economy.accounts = new HashMap<>();
        config.economy.currencies = new HashMap<>();

        GUIShop.config = config;
    }

    private GuiShopConfig.CurrencyDefinition currencyDef(String name, String prefix, String suffix, int decimals) {
        return new GuiShopConfig.CurrencyDefinition(name, prefix, suffix, decimals, ResourceId.ofVanilla("gold_nugget"));
    }

    private GuiShopConfig.AccountDefinition accountDef(String name, String currencyId) {
        return new GuiShopConfig.AccountDefinition(currencyId, name, ResourceId.ofVanilla("iron_ingot"));
    }

    @Test
    void testStaticValues() {
        assertEquals("GuiShop Economy", provider.name().getString());
        assertEquals(GUIShop.MODID, GuiShopEconomyProvider.ID);
    }

    @Test
    void canRegisterProvider() {
        if (eu.pb4.common.economy.api.CommonEconomy.getProvider(GuiShopEconomyProvider.ID) == null) {
            GuiShopEconomyProvider.init();
        }

        assertSame(
            GuiShopEconomyProvider.INSTANCE,
            eu.pb4.common.economy.api.CommonEconomy.getProvider(GuiShopEconomyProvider.ID)
        );
    }

    // -------------------------------------------------------------------------
    // getCurrency()
    // -------------------------------------------------------------------------

    @Test
    void getCurrencyReturnsNullIfDisabled() {
        GUIShop.config.economyDisabled = true;

        assertNull(provider.getCurrency(null, "coins"));
    }

    @Test
    void getCurrencyReturnsNullIfNotDefined() {
        assertNull(provider.getCurrency(null, "missing"));
    }

    @Test
    void getCurrencyReturnsCorrectInstance() {
        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.currencies.put(
            "coins",
            currencyDef("Coins", "$", "", 0)
        );
        GUIShop.config.economy.currencies.put(
            "nuggets",
            currencyDef("Nuggets", "", " Nuggets", 0)
        );

        EconomyCurrency cur = provider.getCurrency(null, "nuggets");

        assertNotNull(cur);
        assertEquals(Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "nuggets"), cur.id());
    }

    // -------------------------------------------------------------------------
    // getCurrencies()
    // -------------------------------------------------------------------------

    @Test
    void getCurrenciesReturnsEmptyWhenNoCurrenciesDefined() {
        assertTrue(provider.getCurrencies(null).isEmpty());
    }

    @Test
    void getCurrenciesReturnsAllDefinedCurrencies() {
        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.currencies.put("coins", currencyDef("Coins", "$", "", 0));
        GUIShop.config.economy.currencies.put("gems", currencyDef("Gems", "G", "", 2));

        Collection<EconomyCurrency> currencies = provider.getCurrencies(null);

        assertEquals(2, currencies.size());
    }

    // -------------------------------------------------------------------------
    // getAccount()
    // -------------------------------------------------------------------------

    @Test
    void getAccountReturnsNullIfDisabled() {
        GUIShop.config.economyDisabled = true;
        assertNull(provider.getAccount(null, profile, "main"));
    }

    @Test
    void getAccountReturnsNullIfNotDefined() {
        assertNull(provider.getAccount(null, profile, "missing"));
    }

    @Test
    void getAccountReturnsCorrectInstance() {
        Identifier curId = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "coins");

        GuiShopConfig.AccountDefinition def = accountDef("Wallet", curId.getPath());

        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.currencies.put(curId.getPath(), currencyDef("Coins", "$", "", 0));
        GUIShop.config.economy.accounts.put("wallet", def);

        EconomyAccount acc = provider.getAccount(null, profile, "wallet");

        assertNotNull(acc);
        assertEquals(Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "wallet"), acc.id());
        assertEquals(MinecraftCompat.id(profile), acc.owner());

        assertEquals(curId, acc.currency().id());
    }

    // -------------------------------------------------------------------------
    // getAccounts()
    // -------------------------------------------------------------------------

    @Test
    void getAccountsReturnsEmptyWhenNoAccountsDefined() {
        assertTrue(provider.getAccounts(null, profile).isEmpty());
    }

    @Test
    void getAccountsReturnsAllDefinedAccounts() {
        ResourceId curId = ResourceId.of(GuiShopEconomyProvider.ID, "coins");

        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.accounts.put("wallet", accountDef("Wallet", curId.path()));
        GUIShop.config.economy.accounts.put("bank", accountDef("Bank", curId.path()));

        Collection<EconomyAccount> accounts = provider.getAccounts(null, profile);

        assertEquals(2, accounts.size());
    }

    // -------------------------------------------------------------------------
    // defaultAccount()
    // -------------------------------------------------------------------------

    @Test
    void defaultAccountReturnsNullIfDisabled() {
        GUIShop.config.economyDisabled = true;
        assertNull(provider.defaultAccount(null, profile, new GuiShopEconomyCurrency(
            ResourceId.of(GuiShopEconomyProvider.ID, "coins"),
            currencyDef("Coins", "$", "", 0)
        )));
    }

    @Test
    void defaultAccountReturnsNullIfNoMatchingCurrency() {
        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.accounts.put("wallet", accountDef("Wallet", "coins"));
        GUIShop.config.economy.accounts.put("bank", accountDef("Bank", "gems"));

        EconomyCurrency lookup = new GuiShopEconomyCurrency(
            ResourceId.of(GuiShopEconomyProvider.ID, "emerald"),
            currencyDef("Emerald", "E", "", 1)
        );

        assertNull(provider.defaultAccount(null, profile, lookup));
    }

    @Test
    void defaultAccountReturnsFirstMatchingAccount() {
        ResourceId coins = ResourceId.of(GuiShopEconomyProvider.ID, "coins");

        assertNotNull(GUIShop.config.economy);
        GUIShop.config.economy.accounts.put("wallet", accountDef("Wallet", coins.path()));
        GUIShop.config.economy.accounts.put("bank", accountDef("Bank", coins.path()));

        EconomyCurrency lookup = new GuiShopEconomyCurrency(
            coins,
            currencyDef("Coins", "$", "", 0)
        );

        String result = provider.defaultAccount(null, profile, lookup);

        assertNotNull(result);
        assertTrue(
            result.equals("wallet") || result.equals("bank"),
            "defaultAccount() must return one of the accounts using this currency"
        );
    }

    // -------------------------------------------------------------------------
    // icon()
    // -------------------------------------------------------------------------

    @Test
    void iconReturnsChestItem() {
        ItemStack icon = provider.icon();

        assertNotNull(icon);
        assertEquals(Items.CHEST, icon.getItem());
    }
}
