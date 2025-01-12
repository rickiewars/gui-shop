package unsafedodo.guishop.economy;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface IEconomyService {
    boolean add(UUID uuid, long amount);
    boolean remove(UUID uuid, long amount);
    boolean hasEnoughMoney(UUID uuid, long amount) throws ExecutionException, InterruptedException;
    long getBalance(UUID uuid) throws ExecutionException, InterruptedException;
    boolean transfer(UUID sender, UUID receiver, long amount);
}
