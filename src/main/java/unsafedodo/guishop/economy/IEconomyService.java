package unsafedodo.guishop.economy;

import net.impactdev.impactor.api.economy.accounts.Account;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface IEconomyService {
    boolean add(UUID uuid, double amount);
    boolean remove(UUID uuid, double amount);
    boolean hasEnoughMoney(UUID uuid, double amount) throws ExecutionException, InterruptedException;
    double getBalance(UUID uuid) throws ExecutionException, InterruptedException;
    boolean transfer(UUID sender, UUID receiver, double amount);
}
