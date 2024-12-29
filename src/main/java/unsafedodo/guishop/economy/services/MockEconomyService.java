package unsafedodo.guishop.economy.services;
import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.economy.IEconomyService;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Mock implementation of the IEconomyService interface
 * Use only for testing purposes
 */
public class MockEconomyService implements IEconomyService {
    boolean skipValidation = false;
    boolean successState = true;
    double balanceState = 999999.99;

    public MockEconomyService() {
        GUIShop.LOGGER.debug("MockEconomyService constructor");
        GUIShop.LOGGER.warn(
                "MockEconomyService is being used. " +
                "This is only for testing purposes. " +
                "Please configure a proper economy service when used in production."
        );
    }

    public void MockSuccessState(boolean successState) {
        this.successState = successState;
    }
    public void MockBalanceState(double balanceState) {
        this.balanceState = balanceState;
    }
    public void SkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Override
    public boolean add(UUID uuid, double amount) {
        GUIShop.LOGGER.debug("MockEconomyService.add: " + uuid + " " + amount);
        return skipValidation && successState
                || amount > 0 && uuid != null && successState;

    }

    @Override
    public boolean remove(UUID uuid, double amount) {
        GUIShop.LOGGER.debug("MockEconomyService.remove: " + uuid + " " + amount);
        return skipValidation && successState
                || amount > 0 && uuid != null && successState;
    }

    @Override
    public boolean hasEnoughMoney(UUID uuid, double amount) throws ExecutionException, InterruptedException {
        GUIShop.LOGGER.debug("MockEconomyService.hasEnoughMoney: " + uuid + " " + amount);
        return skipValidation && successState
                || amount > 0 && uuid != null && successState;
    }

    @Override
    public double getBalance(UUID uuid) throws ExecutionException, InterruptedException {
        GUIShop.LOGGER.debug("MockEconomyService.getBalance: " + uuid);
        return balanceState;
    }

    @Override
    public boolean transfer(UUID sender, UUID receiver, double amount) {
        GUIShop.LOGGER.debug("MockEconomyService.transfer: " + sender + " " + receiver + " " + amount);
        return skipValidation && successState
                || amount > 0 && sender != null && receiver != null && successState;
    }
}
