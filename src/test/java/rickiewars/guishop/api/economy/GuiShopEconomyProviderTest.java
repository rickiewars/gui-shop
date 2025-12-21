package rickiewars.guishop.api.economy;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.config.EconomyConfig;

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

        EconomyConfig config = new EconomyConfig();
        config.disabled = false;

        config.economy = new EconomyConfig.EconomyProviderDefinition();
        config.economy.accounts = new HashMap<>();
        config.economy.currencies = new HashMap<>();

        GUIShop.economyConfig = config;
    }

    private EconomyConfig.CurrencyDefinition currencyDef(String name, String prefix, String suffix, int decimals) {
        return new EconomyConfig.CurrencyDefinition(name, prefix, suffix, decimals, new ItemStack(Items.GOLD_NUGGET));
    }

    private EconomyConfig.AccountDefinition accountDef(String name, String currencyId) {
        return new EconomyConfig.AccountDefinition(currencyId, name, new ItemStack(Items.IRON_INGOT));
    }

    @Test
    void testStaticValues() {
        assertEquals("GuiShop Economy", provider.name().getString());
        assertEquals(GUIShop.MODID, GuiShopEconomyProvider.ID);
    }

    @Test
    void canRegisterProvider() {
        GuiShopEconomyProvider.init();

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
        GUIShop.economyConfig.disabled = true;

        assertNull(provider.getCurrency(null, "coins"));
    }

    @Test
    void getCurrencyReturnsNullIfNotDefined() {
        assertNull(provider.getCurrency(null, "missing"));
    }

    @Test
    void getCurrencyReturnsCorrectInstance() {
        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.currencies.put(
            "coins",
            currencyDef("Coins", "$", "", 0)
        );
        GUIShop.economyConfig.economy.currencies.put(
            "nuggets",
            currencyDef("Nuggets", "", " Nuggets", 0)
        );

        EconomyCurrency cur = provider.getCurrency(null, "nuggets");

        assertNotNull(cur);
        assertEquals(Identifier.of(GuiShopEconomyProvider.ID, "nuggets"), cur.id());
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
        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.currencies.put("coins", currencyDef("Coins", "$", "", 0));
        GUIShop.economyConfig.economy.currencies.put("gems", currencyDef("Gems", "G", "", 2));

        Collection<EconomyCurrency> currencies = provider.getCurrencies(null);

        assertEquals(2, currencies.size());
    }

    // -------------------------------------------------------------------------
    // getAccount()
    // -------------------------------------------------------------------------

    @Test
    void getAccountReturnsNullIfDisabled() {
        GUIShop.economyConfig.disabled = true;
        assertNull(provider.getAccount(null, profile, "main"));
    }

    @Test
    void getAccountReturnsNullIfNotDefined() {
        assertNull(provider.getAccount(null, profile, "missing"));
    }

    @Test
    void getAccountReturnsCorrectInstance() {
        Identifier curId = Identifier.of(GuiShopEconomyProvider.ID, "coins");

        EconomyConfig.AccountDefinition def = accountDef("Wallet", curId.getPath());

        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.currencies.put(curId.getPath(), currencyDef("Coins", "$", "", 0));
        GUIShop.economyConfig.economy.accounts.put("wallet", def);

        EconomyAccount acc = provider.getAccount(null, profile, "wallet");

        assertNotNull(acc);
        assertEquals(Identifier.of(GuiShopEconomyProvider.ID, "wallet"), acc.id());
        assertEquals(profile.getId(), acc.owner());

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
        Identifier curId = Identifier.of(GuiShopEconomyProvider.ID, "coins");

        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.accounts.put("wallet", accountDef("Wallet", curId.getPath()));
        GUIShop.economyConfig.economy.accounts.put("bank", accountDef("Bank", curId.getPath()));

        Collection<EconomyAccount> accounts = provider.getAccounts(null, profile);

        assertEquals(2, accounts.size());
    }

    // -------------------------------------------------------------------------
    // defaultAccount()
    // -------------------------------------------------------------------------

    @Test
    void defaultAccountReturnsNullIfDisabled() {
        GUIShop.economyConfig.disabled = true;
        assertNull(provider.defaultAccount(null, profile, new GuiShopEconomyCurrency(
            Identifier.of(GuiShopEconomyProvider.ID, "coins"),
            currencyDef("Coins", "$", "", 0)
        )));
    }

    @Test
    void defaultAccountReturnsNullIfNoMatchingCurrency() {
        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.accounts.put("wallet", accountDef("Wallet", "coins"));
        GUIShop.economyConfig.economy.accounts.put("bank", accountDef("Bank", "gems"));

        EconomyCurrency lookup = new GuiShopEconomyCurrency(
            Identifier.of(GuiShopEconomyProvider.ID, "emerald"),
            currencyDef("Emerald", "E", "", 1)
        );

        assertNull(provider.defaultAccount(null, profile, lookup));
    }

    @Test
    void defaultAccountReturnsFirstMatchingAccount() {
        Identifier coins = Identifier.of(GuiShopEconomyProvider.ID, "coins");

        assertNotNull(GUIShop.economyConfig.economy);
        GUIShop.economyConfig.economy.accounts.put("wallet", accountDef("Wallet", coins.getPath()));
        GUIShop.economyConfig.economy.accounts.put("bank", accountDef("Bank", coins.getPath()));

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
