package rickiewars.guishop.sql;

public interface DatabaseManager {
    void addPlayer(String currency, String uuid, String name);
    void updateName(String currency, String uuid, String name);
    String getNameFromUUID(String currency, String uuid);

    int getBalanceFromUUID(String currency, String uuid);
    int getBalanceFromName(String currency, String name);
    boolean setBalance(String currency, String uuid, int money);
    void setAllBalance(String currency, int money);
    boolean changeBalance(String currency, String uuid, int money);
    void changeAllBalance(String currency, int money);

    String top(String currency, String uuid, int topAmount);
    String rank(String currency, int rank);
    int playerRank(String currency, String uuid);
}
