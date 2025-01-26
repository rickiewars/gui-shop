package rickiewars.guishop.config;

import java.util.HashMap;
import java.util.Map;

public class BaseEconomyConfig<T, U> {
    public Map<String, T> currencies;
    public Map<String, U> accounts;

    public BaseEconomyConfig(Map<String, T> currencies, Map<String, U> accounts) {
        this.currencies = currencies;
        this.accounts = accounts;
    }
    public BaseEconomyConfig() {
        this(new HashMap<>(), new HashMap<>());
    }

    public boolean isConfigured() {
        return !currencies.isEmpty() && !accounts.isEmpty();
    }
}
