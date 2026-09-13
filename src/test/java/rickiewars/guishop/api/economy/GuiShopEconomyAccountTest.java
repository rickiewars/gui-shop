package rickiewars.guishop.api.economy;

import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.economy.impl.EconomyCompat;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.impl.ItemRegistry;
import rickiewars.guishop.config.GuiShopConfig;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GuiShopEconomyAccountTest extends EconomyTest {

    @Test
    void idReturnsConstructorValue() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), UUID.randomUUID()
        );
        assertEquals(economy.accountCardId.toIdentifier(), account.id());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.path()), UUID.randomUUID()
        );
        assertEquals(economy.accountPouchId.toIdentifier(), account2.id());
    }

    @Test
    void ownerReturnsGivenUUID() {
        UUID uuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), uuid
        );
        assertEquals(uuid, account.owner());
    }

    @Test
    void nameReturnsDefinitionName() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), UUID.randomUUID()
        );
        assertEquals(Component.nullToEmpty("Credit card"), account.name());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.path()), UUID.randomUUID()
        );
        assertEquals(Component.nullToEmpty("Pouch"), account2.name());
    }

    @Test
    void accountIconReturnsDefinitionIcon() {
        GuiShopConfig.AccountDefinition config = economy.accounts.get(economy.accountCardId.path());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, config, UUID.randomUUID()
        );
        assertEquals(config.icon, ItemRegistry.idOf(account.accountIcon().getItem()));

        GuiShopConfig.AccountDefinition config2 = economy.accounts.get(economy.accountPouchId.path());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, config2, UUID.randomUUID()
        );
        assertEquals(config2.icon, ItemRegistry.idOf(account2.accountIcon().getItem()));
    }

    @Test
    void providerReturnsGuiShopEconomyProvider() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), UUID.randomUUID()
        );
        assertSame(GuiShopEconomyProvider.INSTANCE, account.provider());
    }

    @Test
    void currencyReturnsResolvedCurrency() {
        GuiShopConfig.CurrencyDefinition config = economy.currencies.get(economy.currencyCreditsId.path());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), UUID.randomUUID()
        );
        assertEquals(economy.currencyCreditsId.toIdentifier(), account.currency().id());
        assertEquals(Component.nullToEmpty(config.name), account.currency().name());

        GuiShopConfig.CurrencyDefinition config2 = economy.currencies.get(economy.currencyCoinsId.path());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.path()), UUID.randomUUID()
        );
        assertEquals(economy.currencyCoinsId.toIdentifier(), account2.currency().id());
        assertEquals(Component.nullToEmpty(config2.name), account2.currency().name());
    }

    @Test
    void balanceReturnsValueFromDatabase() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 123);
        fakeDb.setBalance(economy.currencyCoinsId.toString(), playerUuid.toString(), 456);

        GuiShopEconomyAccount account1 = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );
        assertEquals(123, EconomyCompat.balance(account1).longValueExact());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.path()), playerUuid
        );
        assertEquals(456, EconomyCompat.balance(account2).longValueExact());
    }

    @Test
    void setBalanceUpdatesDatabase() {
        UUID playerUuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.path()), playerUuid
        );

        EconomyCompat.setBalance(account, BigInteger.valueOf(500));
        EconomyCompat.setBalance(account2, BigInteger.valueOf(30));

        assertEquals(500, fakeDb.getBalance(economy.currencyCreditsId.toString(), playerUuid.toString()));
        assertEquals(30, fakeDb.getBalance(economy.currencyCoinsId.toString(), playerUuid.toString()));
    }

    @Test
    void canIncreaseBalanceSucceedsWhenBelowMax() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 200);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );

        EconomyTransaction tx = EconomyCompat.canIncreaseBalance(account, BigInteger.valueOf(500));

        assertTrue(tx.isSuccessful());
        assertFalse(tx.isFailure());
        assertEquals(200, EconomyCompat.previousBalance(tx).longValueExact());
        assertEquals(700, EconomyCompat.finalBalance(tx).longValueExact());
        assertEquals(500, EconomyCompat.transactionAmount(tx).longValueExact());
    }

    @Test
    void canIncreaseBalanceFailsWhenExceedingMaxLong() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), Long.MAX_VALUE - 1);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );

        EconomyTransaction tx = EconomyCompat.canIncreaseBalance(account, BigInteger.valueOf(2));

        assertFalse(tx.isSuccessful());
        assertTrue(tx.isFailure());
        assertEquals(Long.MAX_VALUE - 1, EconomyCompat.previousBalance(tx).longValueExact());
        assertEquals(Long.MAX_VALUE - 1, EconomyCompat.finalBalance(tx).longValueExact());
        assertEquals(0, EconomyCompat.transactionAmount(tx).longValueExact());
    }

    @Test
    void canDecreaseBalanceFailsWhenGoingNegative() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 20);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );

        EconomyTransaction tx = EconomyCompat.canDecreaseBalance(account, BigInteger.valueOf(21));

        assertFalse(tx.isSuccessful());
        assertTrue(tx.isFailure());
        assertEquals(20, EconomyCompat.previousBalance(tx).longValueExact());
        assertEquals(20, EconomyCompat.finalBalance(tx).longValueExact());
        assertEquals(0, EconomyCompat.transactionAmount(tx).longValueExact());
    }

    @Test
    void canDecreaseBalanceSucceedsWhenEnoughBalance() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 100);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.path()), playerUuid
        );

        EconomyTransaction tx = EconomyCompat.canDecreaseBalance(account, BigInteger.valueOf(40));

        assertTrue(tx.isSuccessful());
        assertFalse(tx.isFailure());
        assertEquals(100, EconomyCompat.previousBalance(tx).longValueExact());
        assertEquals(60, EconomyCompat.finalBalance(tx).longValueExact());
        assertEquals(40, EconomyCompat.transactionAmount(tx).longValueExact());
    }
}
