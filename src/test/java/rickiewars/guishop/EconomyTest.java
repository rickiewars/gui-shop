package rickiewars.guishop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import rickiewars.guishop.api.database.impl.FakeDatabaseManager;
import rickiewars.guishop.util.TestUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class EconomyTest extends MinecraftTest {
    protected FakeDatabaseManager fakeDb = new FakeDatabaseManager();
    protected TestUtils.EconomyDetails economy;

    @BeforeEach
    final void setupEconomy() {
        economy = TestUtils.initTestEconomy(fakeDb);
    }
}
