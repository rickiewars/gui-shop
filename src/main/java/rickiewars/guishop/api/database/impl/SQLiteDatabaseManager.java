package rickiewars.guishop.api.database.impl;

import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.config.EconomyConfig;
import rickiewars.guishop.util.CommonMethods;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteDatabaseManager implements DatabaseManager {
    private final String url;

    public SQLiteDatabaseManager(EconomyConfig config) {
        if (config.database == null
            || config.database.fileLocation == null
            || config.database.fileLocation.isEmpty()
        ) {
            GUIShop.LOGGER.error("Database file location not set in config");
            this.url = null;
            return;
        }
        File file = new File(config.database.fileLocation);
        url = "jdbc:sqlite:" + file.getPath().replace('\\', '/');

        initTables();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(this.url);
        } catch (SQLException e) {
            GUIShop.LOGGER.error(e.getMessage());
        }
        return conn;
    }

    // Initializes the database with registered tables
    private void initTables() {
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

    public void updateAccount(String currency, String uuid, String name) {
        String sql = """
            INSERT INTO accounts (uuid, currency, name, balance)
            VALUES (?, ?, ?, 0)
            ON CONFLICT(uuid, currency)
            DO UPDATE SET name = excluded.name;
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, currency);
            ps.setString(3, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating account name for " + uuid + " and Currency: " + currency, e);
        }
    }

    public String getName(String currency, String uuid) {
        String sql = "SELECT name FROM accounts WHERE uuid = ? AND currency = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.setString(2, currency);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;
            return rs.getString("name");

        } catch (SQLException e) {
            throw new RuntimeException("Error getting player name for UUID " + uuid + " and Currency: " + currency, e);
        }
    }

    public int getBalance(String currency, String uuid){
        String sql = "SELECT uuid, balance FROM accounts WHERE uuid = ? AND currency = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
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

    public boolean setBalance(String currency, String uuid, int amount) {
        if (amount < 0 || amount == Integer.MAX_VALUE) return false;

        try (Connection conn = this.connect()) {
            try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE accounts SET balance = ?
                WHERE uuid = ? AND currency = ?
                """)) {
                ps.setInt(1, amount);
                ps.setString(2, uuid);
                ps.setString(3, currency);

                int updated = ps.executeUpdate();
                if (updated > 0) return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error setting balance for UUID: " + uuid + " and Currency: " + currency, e
            );
        }
        return false;
    }

    public void setAllBalance(String currency, int amount) {
        if (amount < 0 || amount == Integer.MAX_VALUE) return;

        try (Connection conn = this.connect();
            PreparedStatement ps = conn.prepareStatement("""
                UPDATE accounts SET balance = ?
                WHERE currency = ?
                """)
        ) {
            ps.setInt(1, amount);
            ps.setString(2, currency);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error setting all balances for currency " + currency, e);
        }
    }

    public boolean changeBalance(String currency, String uuid, int amount) {
        int oldBal = getBalance(currency, uuid);
        int newBal = oldBal + amount;

        if (newBal < 0 || newBal == Integer.MAX_VALUE) return false;

        return setBalance(currency, uuid, newBal);
    }

    public void changeAllBalance(String currency, int amount) {
        try (Connection conn = this.connect();
            PreparedStatement ps = conn.prepareStatement("""
                UPDATE accounts
                   SET balance = balance + ?
                WHERE balance + ? >= 0
                   AND balance + ? < ?
                   AND currency = ?
                """)
        ) {
            ps.setInt(1, amount);
            ps.setInt(2, amount);
            ps.setInt(3, amount);
            ps.setInt(4, Integer.MAX_VALUE);
            ps.setString(5, currency);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error changing all balances for currency " + currency, e);
        }
    }

    public List<Map<String, Object>> top(String currency, int page) {
        int limit = 20;
        int offset = (page - 1) * limit;
        String sql = """
            SELECT uuid, name, balance
            FROM accounts
            WHERE currency = ?
            ORDER BY balance DESC
            LIMIT ? OFFSET ?
            """;
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currency);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            ResultSet rs = ps.executeQuery();

            int index = offset + 1;

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("rank", index++);
                entry.put("uuid", rs.getString("uuid"));
                entry.put("name", rs.getString("name"));
                entry.put("balance", rs.getInt("balance"));
                results.add(entry);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving top for currency " + currency, e);
        }

        return results;
    }

    public Map<String, String> rank(String currency, int rank) {
        String sql = """
            SELECT uuid, name
            FROM accounts
            WHERE currency = ?
            ORDER BY balance DESC
            LIMIT 1 OFFSET ?
            """;

        try (Connection conn = this.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currency);
            ps.setInt(2, rank - 1);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, String> result = new HashMap<>();
                result.put("uuid", rs.getString("uuid"));
                result.put("name", rs.getString("name"));
                return result;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving rank " + rank + " for currency " + currency, e);
        }
    }

    public int playerRank(String currency, String uuid) {
        String sql = """
            SELECT rank FROM (
                SELECT accounts.uuid,
                       ROW_NUMBER() OVER (ORDER BY accounts.balance DESC) AS rank
                FROM accounts
                WHERE currency = ?
            ) WHERE uuid = ?;
            """;

        try (Connection conn = this.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currency);
            ps.setString(2, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("rank");
            }

            return 0; // No account yet → rank 0

        } catch (SQLException e) {
            throw new RuntimeException("Error determining player rank for " + uuid, e);
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
