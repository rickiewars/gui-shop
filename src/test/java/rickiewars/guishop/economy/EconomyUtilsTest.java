package rickiewars.guishop.economy;

import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.impl.FakeDatabaseManager;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EconomyUtilsTest {

    private static EconomyCurrency currency(String id) {
        EconomyCurrency currency = mock(EconomyCurrency.class);
        when(currency.id()).thenReturn(Identifier.parse(id));
        return currency;
    }

    @Test
    void registersAccountForEveryCurrency() {
        FakeDatabaseManager db = new FakeDatabaseManager();

        EconomyUtils.registerAccounts(
            db,
            List.of(currency("guishop:credits"), currency("guishop:gems")),
            "uuid-1",
            "Steve"
        );

        assertEquals(
            Map.of("guishop:credits", "Steve", "guishop:gems", "Steve"),
            db.updatedAccounts
        );
    }

    @Test
    void registersNothingWhenNoCurrenciesExist() {
        FakeDatabaseManager db = new FakeDatabaseManager();

        EconomyUtils.registerAccounts(db, List.of(), "uuid-1", "Steve");

        assertTrue(db.updatedAccounts.isEmpty());
    }

    @Test
    void registersNothingWhenEconomyDisabled() {
        GUIShop.config.economyDisabled = true;
        FakeDatabaseManager db = new FakeDatabaseManager();

        EconomyUtils.registerAccounts(
            db,
            List.of(currency("guishop:credits")),
            "uuid-1",
            "Steve"
        );

        assertTrue(db.updatedAccounts.isEmpty());
    }

    @AfterEach
    void resetConfig() {
        GUIShop.config.economyDisabled = false;
    }
}
