package unsafedodo.guishop.economy.services;

import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import unsafedodo.guishop.economy.IEconomyService;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ImpactorEconomyService implements IEconomyService {

    private EconomyService getService() {
        return EconomyService.instance();
    }

    private Currency getCurrency() {
        return getService().currencies().primary();
    }

    private Account getAccount(UUID uuid) {
        return getService().account(getCurrency(), uuid).join();
    }

    @Override
    public boolean add(UUID uuid, double amount) {
        EconomyTransaction transaction = this.getAccount(uuid).deposit(new BigDecimal(amount));
        return transaction.successful();
    }

    @Override
    public boolean remove(UUID uuid, double amount) {
        EconomyTransaction transaction = this.getAccount(uuid).withdraw(new BigDecimal(amount));
        return transaction.successful();
    }

    @Override
    public boolean hasEnoughMoney(UUID uuid, double amount) throws ExecutionException, InterruptedException {
        return getBalance(uuid) >= amount;
    }

    @Override
    public double getBalance(UUID uuid) throws ExecutionException, InterruptedException {
        return this.getAccount(uuid).balance().doubleValue();
    }

    @Override
    public boolean transfer(UUID sender, UUID receiver, double amount) {
        boolean removedMoney = remove(sender, amount);
        boolean addMoney = add(receiver, amount);

        if (!removedMoney && addMoney) {
            remove(receiver, amount);
        }

        if (removedMoney && !addMoney) {
            add(sender, amount);
        }

        return removedMoney && addMoney;
    }
}
