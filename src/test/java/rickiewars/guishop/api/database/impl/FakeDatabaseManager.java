package rickiewars.guishop.api.database.impl;

import rickiewars.guishop.api.database.DatabaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeDatabaseManager implements DatabaseManager {
    private final Map<String, Integer> balances = new HashMap<>();
    public final Map<String, String> updatedAccounts = new HashMap<>();

    private String key(String currency, String uuid) {
        return currency + "|" + uuid;
    }

    @Override
    public int getBalance(String currency, String uuid) {
        return balances.getOrDefault(key(currency, uuid), 0);
    }

    @Override
    public boolean setBalance(String currency, String uuid, int amount) {
        balances.put(key(currency, uuid), amount);
        return true;
    }

    // Not used in tests
    @Override
    public void setAllBalance(String currency, int amount) {}

    @Override
    public boolean changeBalance(String currency, String uuid, int amount) {
        int oldVal = getBalance(currency, uuid);
        balances.put(key(currency, uuid), oldVal + amount);
        return true;
    }

    @Override
    public void changeAllBalance(String currency, int amount) {}

    @Override
    public List<Map<String, Object>> top(String currency, int topAmount) {
        return List.of();
    }

    @Override
    public Map<String, String> rank(String currency, int rank) {
        return Map.of();
    }

    @Override
    public int playerRank(String currency, String uuid) {
        return 0;
    }

    @Override
    public void updateAccount(String currency, String uuid, String name) {
        updatedAccounts.put(currency, name);
    }

    @Override
    public String getName(String currency, String uuid) {
        return "TestUser";
    }
}
