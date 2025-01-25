package rickiewars.guishop.sql;

import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;
import rickiewars.guishop.util.CommonMethods;

import java.io.File;
import java.sql.*;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SQLiteDatabaseManager implements DatabaseManager {
    public static String url;

    public static void initDatabase(Config config) {
        if (config.database.fileLocation == null || config.database.fileLocation.isEmpty()) {
            GUIShop.LOGGER.error("Database file location not set in config");
            return;
        }
        File file = new File(config.database.fileLocation);
        url = "jdbc:sqlite:" + file.getPath().replace('\\', '/');

        initTables();
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            GUIShop.LOGGER.error(e.getMessage());
        }
        return conn;
    }

    // Initializes the database with registered tables
    private static void initTables() {
        Connection conn = connect();
        if (conn == null) {
            return;
        }

        String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
            "uuid text NOT NULL," +
            "currency text NOT NULL," +
            "name text NOT NULL," +
            "balance integer DEFAULT 0," +
            "PRIMARY KEY (uuid, currency)" +
        ");";

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing SQLite tables for GuiShop", e);
        }
    }

    public void addPlayer(String currency, String uuid, String name) {
        String sql = "INSERT INTO accounts(uuid,currency,name,balance) VALUES(?,?,?,?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, uuid);
            pstmt.setString(2, currency);
            pstmt.setString(3, name);
            pstmt.setInt(4, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            updateName(currency, uuid, name);
        }
    }

    public void updateName(String currency, String uuid, String name) {
        String sql = "UPDATE accounts SET name = ? WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, uuid);
            pstmt.setString(3, currency);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating name for UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    public int getBalanceFromUUID(String currency, String uuid){
        String sql = "SELECT uuid, balance FROM accounts WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, uuid);
            pstmt.setString(2, currency);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertFound(rs, currency, uuid);
            return rs.getInt("balance");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting balance from UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    public String getNameFromUUID(String currency, String uuid){
        String sql = "SELECT uuid, name FROM accounts WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, uuid);
            pstmt.setString(2, currency);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertFound(rs, currency, uuid);
            return rs.getString("name");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting name from UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    public int getBalanceFromName(String currency, String name){
        String sql = "SELECT name, balance FROM accounts WHERE name = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, name);
            pstmt.setString(2, currency);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertFound(rs, currency, name);
            return rs.getInt("balance");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting balance from name: " + name + " and Currency: " + currency, e
            );
        }
    }

    public boolean setBalance(String currency, String uuid, int amount) {
        String sql = "UPDATE accounts SET balance = ? WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (amount >= 0 && amount < Integer.MAX_VALUE) {
                pstmt.setInt(1, amount);
                pstmt.setString(2, uuid);
                pstmt.setString(3, currency);
                pstmt.executeUpdate();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error setting balance for UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    public void setAllBalance(String currency, int amount) {
        String sql = "UPDATE accounts SET balance = ? WHERE currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (amount >= 0 && amount < Integer.MAX_VALUE) {
                pstmt.setInt(1, amount);
                pstmt.setString(2, currency);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error setting all balances for Currency: " + currency, e
            );
        }
    }

    public boolean changeBalance(String currency, String uuid, int amount) {
        String sql = "UPDATE accounts SET balance = ? WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int bal = getBalanceFromUUID(currency, uuid);
            if (bal + amount >= 0 && bal + amount < Integer.MAX_VALUE) {
                pstmt.setInt(1, bal + amount);
                pstmt.setString(2, uuid);
                pstmt.setString(3, currency);
                pstmt.executeUpdate();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error changing balance for UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    public void changeAllBalance(String currency, int amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE ? > balance + ? AND 0 <= balance + ? AND currency = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, Integer.MAX_VALUE);
            pstmt.setInt(3, amount);
            pstmt.setInt(4, amount);
            pstmt.setString(5, currency);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error changing all balances for Currency: " + currency, e
            );
        }
    }

    public String top(String currency, String uuid, int page){
        String sql = "SELECT uuid, name, balance FROM accounts WHERE currency = ? ORDER BY balance DESC";
        String rankings = "";
        int i = 0;
        int playerRank = 0;
        int repeats = 0;

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next() && (repeats < 10 || playerRank == 0)) {
                if (repeats / 10 + 1 == page) {
                    rankings = rankings.concat(rs.getRow() + ") " + rs.getString("name") + ": $" + rs.getInt("balance") + "\n");
                    i++;
                }
                repeats++;
                if (uuid.equals(rs.getString("uuid"))) {
                    playerRank = repeats;
                }
            }
            if (i < 10) {
                rankings = rankings.concat("---End--- \n");
            }
            return rankings.concat("Your rank is: " + playerRank);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting top players for Currency: " + currency, e
            );
        }
    }

    public String rank(String currency, int rank){
        int repeats = 1;
        String sql = "SELECT name FROM accounts WHERE currency = ? ORDER BY balance DESC";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next() ) {
                if (repeats == rank) {
                    return rs.getString("name");
                }
                repeats++;
            }
            return "No Player";
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting player rank for Currency: " + currency, e
            );
        }
    }

    public int playerRank(String currency, String uuid){
        String sql = "SELECT uuid FROM accounts WHERE currency = ? ORDER BY balance DESC";
        int repeats = 1;

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next() ) {
                if (rs.getString("uuid").equals(uuid)) {
                    return repeats;
                }
                repeats++;
            }
            return repeats;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error getting player rank for UUID: " + uuid + " and Currency: " + currency, e
            );
        }
    }

    private static void assertFound(ResultSet rs, String currency, String uuid) throws SQLException, NoSuchElementException {
        if (rs.isClosed()) throwNotFoundError(currency, uuid);
    }
    private static void throwNotFoundError(String currency, String uuid) throws NoSuchElementException {
        throw new NoSuchElementException(
                "Could not find account for " + CommonMethods.translatePlayer(UUID.fromString(uuid))
                + " using the currency " + currency
        );
    }
}
