package rickiewars.guishop.api.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import rickiewars.guishop.api.database.impl.SQLiteDatabaseManager;
import rickiewars.guishop.config.GuiShopConfig;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SQLiteDatabaseManagerTest {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File dbFile;
    private SQLiteDatabaseManager db;

    @BeforeEach
    void setup() throws Exception {
        dbFile = Files.createTempFile("guishop-", ".sqlite").toFile();
        dbFile.deleteOnExit();

        GuiShopConfig cfg = new GuiShopConfig();
        cfg.database = new GuiShopConfig.DatabaseConfig(
            GuiShopConfig.DatabaseConfig.DatabaseType.SQLITE,
            dbFile.getAbsolutePath()
        );

        db = new SQLiteDatabaseManager(cfg);
    }

    @AfterEach
    void cleanup() {
        if (dbFile.exists() && !dbFile.delete()) {
            System.err.println("Warning: Failed to delete " + dbFile.getAbsolutePath());
        }
    }

    // -------------------------------------------------------------------------
    // ADD PLAYER / UPDATE NAME
    // -------------------------------------------------------------------------

    @Test
    void canAddPlayer() {
        String currency = "coins";
        String uuid = UUID.randomUUID().toString();
        db.updateAccount(currency, uuid, "Alice");
        assertEquals("Alice", db.getName(currency, uuid));
    }

    @Test
    void addExistingPlayerDuplicateTriggersUpdateName() {
        String currency = "coins";
        String uuid = UUID.randomUUID().toString();
        db.updateAccount(currency, uuid, "OldName");
        db.updateAccount(currency, uuid, "NewName");
        assertEquals("NewName", db.getName(currency, uuid));
    }

    @Test
    void balanceCanBeStoredInMultipleCurrencies() {
        String currency1 = "coins";
        String currency2 = "coins2";
        String uuid = UUID.randomUUID().toString();

        db.updateAccount(currency1, uuid, "Bob");
        db.updateAccount(currency2, uuid, "Bob");

        db.setBalance(currency1, uuid, 123);
        db.setBalance(currency2, uuid, 456);

        assertEquals(123, db.getBalance(currency1, uuid));
        assertEquals(456, db.getBalance(currency2, uuid));
    }

    @Test
    void updatingAccountNameDoesNotAffectBalance() {
        String currency = "coins";
        String uuid = UUID.randomUUID().toString();

        db.updateAccount(currency, uuid, "Alice");
        db.setBalance(currency, uuid, 100);
        db.updateAccount(currency, uuid, "Bob");

        assertEquals(100, db.getBalance(currency, uuid));
        assertEquals("Bob", db.getName(currency, uuid));
    }

    @Test
    void changeBalanceAddsAndRejectsOverflow() {
        String currency = "coins";
        String uuid = UUID.randomUUID().toString();
        db.updateAccount(currency, uuid, "Player");

        db.setBalance(currency, uuid, 100);
        assertTrue(db.changeBalance(currency, uuid, 50));
        assertEquals(150, db.getBalance(currency, uuid));

        assertFalse(db.changeBalance(currency, uuid, Integer.MAX_VALUE));
        assertEquals(150, db.getBalance(currency, uuid));
    }

    @Test
    void changeBalanceSubtractsAndRejectsOverflow() {
        String currency = "coins";
        String uuid = UUID.randomUUID().toString();
        db.updateAccount(currency, uuid, "Player");

        db.setBalance(currency, uuid, 200);
        assertTrue(db.changeBalance(currency, uuid, -50));
        assertEquals(150, db.getBalance(currency, uuid));

        // Overflow: bal + amount >= Integer.MAX_VALUE
        assertFalse(db.changeBalance(currency, uuid, -200));
        assertEquals(150, db.getBalance(currency, uuid));
    }

    @Test
    void setAllBalanceOnlyAffectsGivenCurrency() {
        String currency = "coins";
        String currency2 = "euros";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "One");
        db.updateAccount(currency, u2, "Two");
        db.updateAccount(currency2, u1, "One");
        db.updateAccount(currency2, u2, "Two");

        db.setAllBalance(currency, 100);
        db.setAllBalance(currency2, 200);

        assertEquals(100, db.getBalance(currency, u1));
        assertEquals(100, db.getBalance(currency, u2));
        assertEquals(200, db.getBalance(currency2, u1));
        assertEquals(200, db.getBalance(currency2, u2));
    }

    @Test
    void changeAllBalanceOnlyAffectsGivenCurrency() {
        String currency = "coins";
        String currency2 = "euros";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "One");
        db.updateAccount(currency, u2, "Two");
        db.updateAccount(currency2, u1, "One");
        db.updateAccount(currency2, u2, "Two");

        db.setAllBalance(currency, 100);
        db.setAllBalance(currency2, 200);
        db.changeAllBalance(currency, 200);

        assertEquals(300, db.getBalance(currency, u1));
        assertEquals(300, db.getBalance(currency, u2));
        assertEquals(200, db.getBalance(currency2, u1));
        assertEquals(200, db.getBalance(currency2, u2));
    }

    @Test
    void changeAllBalanceCanSubtractAmount() {
        String currency = "coins";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "One");
        db.updateAccount(currency, u2, "Two");

        db.setAllBalance(currency, 200);
        db.changeAllBalance(currency, -50);

        assertEquals(150, db.getBalance(currency, u1));
        assertEquals(150, db.getBalance(currency, u2));
    }

    // -------------------------------------------------------------------------
    // RANKING SYSTEM
    // -------------------------------------------------------------------------

    @Test
    void playerRankReturnsCorrectOrdering() {
        String currency = "coins";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();
        String u3 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "A");
        db.updateAccount(currency, u2, "B");
        db.updateAccount(currency, u3, "C");

        db.setBalance(currency, u1, 100);
        db.setBalance(currency, u2, 500);
        db.setBalance(currency, u3, 300);

        // SQLite sorts DESC, highest first
        assertEquals(1, db.playerRank(currency, u2)); // 500
        assertEquals(2, db.playerRank(currency, u3)); // 300
        assertEquals(3, db.playerRank(currency, u1)); // 100
    }

    @Test
    void playerRank_ReturnsZeroForUnknownPlayer() {
        assertEquals(0, db.playerRank("coins", "ZZZ-NOT-EXIST"));
    }

    @Test
    void rankReturnsCorrectEntry() {
        String currency = "coins";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "Top");
        db.updateAccount(currency, u2, "Low");

        db.setBalance(currency, u1, 999);
        db.setBalance(currency, u2, 1);

        var r1 = db.rank(currency, 1);
        var r2 = db.rank(currency, 2);

        assertNotNull(r1);
        assertEquals(u1, r1.get("uuid"));
        assertEquals("Top", r1.get("name"));

        assertNotNull(r2);
        assertEquals(u2, r2.get("uuid"));
        assertEquals("Low", r2.get("name"));
    }

    @Test
    void rank_ReturnsNull_WhenRankDoesNotExist() {
        assertNull(db.rank("coins", 999));
    }

    @Test
    void topListsPlayersWithStructuredEntries() {
        String currency = "coins";
        String u1 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "Alice");
        db.setBalance(currency, u1, 1234);

        var list = db.top(currency, 1);
        assertEquals(1, list.size());

        var entry = list.getFirst();
        assertEquals(1, entry.get("rank"));
        assertEquals(u1, entry.get("uuid"));
        assertEquals("Alice", entry.get("name"));
        assertEquals(1234, entry.get("balance"));
    }

    @Test
    void topReturnsCorrectlyOrderedList() {
        String currency = "coins";
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();
        String u3 = UUID.randomUUID().toString();

        db.updateAccount(currency, u1, "Alice");
        db.setBalance(currency, u1, 500);
        db.updateAccount(currency, u2, "Bob");
        db.setBalance(currency, u2, 100);
        db.updateAccount(currency, u3, "Dave");
        db.setBalance(currency, u3, 300);

        List<Map<String,Object>> list = db.top(currency, 1);
        assertEquals(3, list.size());

        Map<String,Object> r1 = list.getFirst();
        Map<String,Object> r2 = list.get(1);
        Map<String,Object> r3 = list.get(2);

        assertEquals(1, r1.get("rank"));
        assertEquals(u1, r1.get("uuid"));
        assertEquals("Alice", r1.get("name"));
        assertEquals(500, r1.get("balance"));

        assertEquals(2, r2.get("rank"));
        assertEquals(u3, r2.get("uuid"));
        assertEquals("Dave", r2.get("name"));
        assertEquals(300, r2.get("balance"));

        assertEquals(3, r3.get("rank"));
        assertEquals(u2, r3.get("uuid"));
        assertEquals("Bob", r3.get("name"));
        assertEquals(100, r3.get("balance"));
    }

    @Test
    void topRespectsPaging() {
        String currency = "coins";

        for (int i = 0; i < 30; i++) {
            String uuid = "U" + i;
            String name = "User " + i;
            db.updateAccount(currency, uuid, name);
            db.setBalance(currency, uuid, 10 * i);
        }

        List<Map<String,Object>> page1 = db.top("coins", 1);
        List<Map<String,Object>> page2 = db.top("coins", 2);

        assertEquals(20, page1.size());
        assertEquals(10, page2.size());

        assertEquals(1, page1.getFirst().get("rank"));
        assertEquals(20, page1.getLast().get("rank"));
        assertEquals(21, page2.getFirst().get("rank"));
        assertEquals(30, page2.getLast().get("rank"));
    }
}
