package rickiewars.guishop.api.database;

import java.util.List;
import java.util.Map;

public interface DatabaseManager {
    void updateAccount(String currency, String uuid, String name);
    String getName(String currency, String uuid);

    long getBalance(String currency, String uuid);
    boolean setBalance(String currency, String uuid, long money);
    void setAllBalance(String currency, long money);
    boolean changeBalance(String currency, String uuid, long money);
    void changeAllBalance(String currency, long money);

    List<Map<String, Object>> top(String currency, int topAmount);
    Map<String, String> rank(String currency, int rank);
    int playerRank(String currency, String uuid);
}
