package rickiewars.guishop.api.economy;

import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.GUIShopTest;
import rickiewars.guishop.api.database.impl.FakeDatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.config.EconomyConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GuiShopEconomyAccountTest {
    FakeDatabaseManager fakeDb = new FakeDatabaseManager();

    Map<String, EconomyConfig.CurrencyDefinition> currencies = new HashMap<>();
    Map<String, EconomyConfig.AccountDefinition> accounts = new HashMap<>();

    Identifier creditsId = Identifier.of(GuiShopEconomyProvider.ID, "credits");
    Identifier coinsId = Identifier.of(GuiShopEconomyProvider.ID, "coins");
    Identifier cardId = Identifier.of(GuiShopEconomyProvider.ID, "card");
    Identifier pouchId = Identifier.of(GuiShopEconomyProvider.ID, "pouch");

    @BeforeEach
    void setup() {
        GUIShopTest.initMinecraft();

        currencies.put(creditsId.getPath(), new EconomyConfig.CurrencyDefinition(
            "Credits", "$", "", 2, new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
        ));
        currencies.put(coinsId.getPath(), new EconomyConfig.CurrencyDefinition(
            "Coins", "", " Coins", 0, new ItemStack(Items.GOLD_NUGGET)
        ));

        accounts.put(cardId.getPath(), new EconomyConfig.AccountDefinition(
            creditsId.getPath(),
            "Credit card",
            new ItemStack(Items.PAPER)
        ));
        accounts.put(pouchId.getPath(), new EconomyConfig.AccountDefinition(
            coinsId.getPath(),
            "Pouch",
            new ItemStack(Items.BROWN_BUNDLE)
        ));

        GUIShop.economyConfig = new EconomyConfig();
        GUIShop.economyConfig.economy = new EconomyConfig.EconomyProviderDefinition(currencies, accounts);
        GUIShop.databaseManager = fakeDb;
    }

    @Test
    void idReturnsConstructorValue() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), UUID.randomUUID()
        );
        assertEquals(cardId, account.id());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, accounts.get(pouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(pouchId, account2.id());
    }

    @Test
    void ownerReturnsGivenUUID() {
        UUID uuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), uuid
        );
        assertEquals(uuid, account.owner());
    }

    @Test
    void nameReturnsDefinitionName() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), UUID.randomUUID()
        );
        assertEquals(Text.of("Credit card"), account.name());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, accounts.get(pouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(Text.of("Pouch"), account2.name());
    }

    @Test
    void accountIconReturnsDefinitionIcon() {
        EconomyConfig.AccountDefinition config = accounts.get(cardId.getPath());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, config, UUID.randomUUID()
        );
        assertEquals(config.icon, account.accountIcon());

        EconomyConfig.AccountDefinition config2 = accounts.get(pouchId.getPath());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, config2, UUID.randomUUID()
        );
        assertEquals(config2.icon, account2.accountIcon());
    }

    @Test
    void providerReturnsGuiShopEconomyProvider() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), UUID.randomUUID()
        );
        assertSame(GuiShopEconomyProvider.INSTANCE, account.provider());
    }

    @Test
    void currencyReturnsResolvedCurrency() {
        EconomyConfig.CurrencyDefinition config = currencies.get(creditsId.getPath());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), UUID.randomUUID()
        );
        assertEquals(creditsId, account.currency().id());
        assertEquals(Text.of(config.name), account.currency().name());

        EconomyConfig.CurrencyDefinition config2 = currencies.get(coinsId.getPath());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, accounts.get(pouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(coinsId, account2.currency().id());
        assertEquals(Text.of(config2.name), account2.currency().name());
    }

    @Test
    void balanceReturnsValueFromDatabase() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(creditsId.toString(), playerUuid.toString(), 123);
        fakeDb.setBalance(coinsId.toString(), playerUuid.toString(), 456);

        GuiShopEconomyAccount account1 = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );
        assertEquals(123, account1.balance());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, accounts.get(pouchId.getPath()), playerUuid
        );
        assertEquals(456, account2.balance());
    }

    @Test
    void setBalanceUpdatesDatabase() {
        UUID playerUuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            pouchId, accounts.get(pouchId.getPath()), playerUuid
        );

        account.setBalance(500);
        account2.setBalance(30);

        assertEquals(500, fakeDb.getBalance(creditsId.toString(), playerUuid.toString()));
        assertEquals(30, fakeDb.getBalance(coinsId.toString(), playerUuid.toString()));
    }

    @Test
    void canIncreaseBalanceSucceedsWhenBelowMax() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(creditsId.toString(), playerUuid.toString(), 200);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );

        EconomyTransaction tx = account.canIncreaseBalance(500);

        assertTrue(tx.isSuccessful());
        assertFalse(tx.isFailure());
        assertEquals(200, tx.previousBalance());
        assertEquals(700, tx.finalBalance());
        assertEquals(500, tx.transactionAmount());
    }

    @Test
    void canIncreaseBalanceFailsWhenExceedingMaxInt() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(creditsId.toString(), playerUuid.toString(), Integer.MAX_VALUE - 1);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );

        EconomyTransaction tx = account.canIncreaseBalance(2);

        assertFalse(tx.isSuccessful());
        assertTrue(tx.isFailure());
        assertEquals(Integer.MAX_VALUE - 1, tx.previousBalance());
        assertEquals(Integer.MAX_VALUE - 1, tx.finalBalance());
        assertEquals(0, tx.transactionAmount());
    }

    @Test
    void canDecreaseBalanceFailsWhenGoingNegative() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(creditsId.toString(), playerUuid.toString(), 20);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );

        EconomyTransaction tx = account.canDecreaseBalance(21);

        assertFalse(tx.isSuccessful());
        assertTrue(tx.isFailure());
        assertEquals(20, tx.previousBalance());
        assertEquals(20, tx.finalBalance());
        assertEquals(0, tx.transactionAmount());
    }

    @Test
    void canDecreaseBalanceSucceedsWhenEnoughBalance() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(creditsId.toString(), playerUuid.toString(), 100);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            cardId, accounts.get(cardId.getPath()), playerUuid
        );

        EconomyTransaction tx = account.canDecreaseBalance(40);

        assertTrue(tx.isSuccessful());
        assertFalse(tx.isFailure());
        assertEquals(100, tx.previousBalance());
        assertEquals(60, tx.finalBalance());
        assertEquals(40, tx.transactionAmount());
    }
}
