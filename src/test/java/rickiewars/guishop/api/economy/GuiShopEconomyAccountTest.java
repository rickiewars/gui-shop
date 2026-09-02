package rickiewars.guishop.api.economy;

import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.EconomyTest;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.config.GuiShopConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GuiShopEconomyAccountTest extends EconomyTest {

    @Test
    void idReturnsConstructorValue() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), UUID.randomUUID()
        );
        assertEquals(economy.accountCardId, account.id());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(economy.accountPouchId, account2.id());
    }

    @Test
    void ownerReturnsGivenUUID() {
        UUID uuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), uuid
        );
        assertEquals(uuid, account.owner());
    }

    @Test
    void nameReturnsDefinitionName() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), UUID.randomUUID()
        );
        assertEquals(Text.of("Credit card"), account.name());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(Text.of("Pouch"), account2.name());
    }

    @Test
    void accountIconReturnsDefinitionIcon() {
        GuiShopConfig.AccountDefinition config = economy.accounts.get(economy.accountCardId.getPath());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, config, UUID.randomUUID()
        );
        assertEquals(config.icon, net.minecraft.registry.Registries.ITEM.getId(account.accountIcon().getItem()));

        GuiShopConfig.AccountDefinition config2 = economy.accounts.get(economy.accountPouchId.getPath());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, config2, UUID.randomUUID()
        );
        assertEquals(config2.icon, net.minecraft.registry.Registries.ITEM.getId(account2.accountIcon().getItem()));
    }

    @Test
    void providerReturnsGuiShopEconomyProvider() {
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), UUID.randomUUID()
        );
        assertSame(GuiShopEconomyProvider.INSTANCE, account.provider());
    }

    @Test
    void currencyReturnsResolvedCurrency() {
        GuiShopConfig.CurrencyDefinition config = economy.currencies.get(economy.currencyCreditsId.getPath());
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), UUID.randomUUID()
        );
        assertEquals(economy.currencyCreditsId, account.currency().id());
        assertEquals(Text.of(config.name), account.currency().name());

        GuiShopConfig.CurrencyDefinition config2 = economy.currencies.get(economy.currencyCoinsId.getPath());
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.getPath()), UUID.randomUUID()
        );
        assertEquals(economy.currencyCoinsId, account2.currency().id());
        assertEquals(Text.of(config2.name), account2.currency().name());
    }

    @Test
    void balanceReturnsValueFromDatabase() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 123);
        fakeDb.setBalance(economy.currencyCoinsId.toString(), playerUuid.toString(), 456);

        GuiShopEconomyAccount account1 = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
        );
        assertEquals(123, account1.balance());

        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.getPath()), playerUuid
        );
        assertEquals(456, account2.balance());
    }

    @Test
    void setBalanceUpdatesDatabase() {
        UUID playerUuid = UUID.randomUUID();
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
        );
        GuiShopEconomyAccount account2 = new GuiShopEconomyAccount(
            economy.accountPouchId, economy.accounts.get(economy.accountPouchId.getPath()), playerUuid
        );

        account.setBalance(500);
        account2.setBalance(30);

        assertEquals(500, fakeDb.getBalance(economy.currencyCreditsId.toString(), playerUuid.toString()));
        assertEquals(30, fakeDb.getBalance(economy.currencyCoinsId.toString(), playerUuid.toString()));
    }

    @Test
    void canIncreaseBalanceSucceedsWhenBelowMax() {
        UUID playerUuid = UUID.randomUUID();
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 200);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
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
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), Integer.MAX_VALUE - 1);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
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
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 20);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
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
        fakeDb.setBalance(economy.currencyCreditsId.toString(), playerUuid.toString(), 100);
        GuiShopEconomyAccount account = new GuiShopEconomyAccount(
            economy.accountCardId, economy.accounts.get(economy.accountCardId.getPath()), playerUuid
        );

        EconomyTransaction tx = account.canDecreaseBalance(40);

        assertTrue(tx.isSuccessful());
        assertFalse(tx.isFailure());
        assertEquals(100, tx.previousBalance());
        assertEquals(60, tx.finalBalance());
        assertEquals(40, tx.transactionAmount());
    }
}
