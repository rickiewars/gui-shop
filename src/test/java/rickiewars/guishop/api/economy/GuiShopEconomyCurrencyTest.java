package rickiewars.guishop.api.economy;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.economy.impl.EconomyCompat;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.GuiShopConfig;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class GuiShopEconomyCurrencyTest extends MinecraftTest {

    private GuiShopConfig.CurrencyDefinition def(int decimals, String prefix, String suffix) {
        return new GuiShopConfig.CurrencyDefinition(
            "Coins",
            prefix,
            suffix,
            decimals,
            ResourceId.ofVanilla("gold_nugget")
        );
    }

    @Test
    void idReturnsConstructorValue() {
        ResourceId id = ResourceId.of("test", "gold");
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(id, def(0, "", ""));

        assertEquals(id.toIdentifier(), currency.id());
    }

    @Test
    void nameReturnsDefinitionName() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(0, "", "")
        );

        assertEquals("Coins", currency.name().getString());
    }

    @Test
    void iconReturnsDefinitionIcon() {
        GuiShopConfig.CurrencyDefinition d = def(0, "$", "");
        d.icon = ResourceId.ofVanilla("gold_nugget");

        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("x", "y"),
            d
        );

        assertEquals(Items.GOLD_NUGGET, currency.icon().getItem());
    }

    @Test
    void providerReturnsGuiShopEconomyProviderInstance() {
        GuiShopConfig.CurrencyDefinition d = def(0, "", "");

        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "coins"),
            d
        );

        assertSame(
            GuiShopEconomyProvider.INSTANCE,
            currency.provider(),
            "Currency.provider() must return the singleton GuiShopEconomyProvider.INSTANCE"
        );
    }

    // formatValue()

    @Test
    void formatValueWithoutDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals("$123", EconomyCompat.formatValue(currency, BigInteger.valueOf(123), false));
    }

    @Test
    void formatNegativeValueReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals("$0", EconomyCompat.formatValue(currency, BigInteger.valueOf(-10), false));
    }

    @Test
    void formatValueWithDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals("$123.45", EconomyCompat.formatValue(currency, BigInteger.valueOf(12345), false));
        assertEquals("$0.05", EconomyCompat.formatValue(currency, BigInteger.valueOf(5), false));
    }

    @Test
    void formatNegativeValueWithDecimalsReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals("$0.00", EconomyCompat.formatValue(currency, BigInteger.valueOf(-12345), false));
    }

    @Test
    void formatValueWithPrefixAndSuffix() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "€", " EUR")
        );

        assertEquals("€12.34 EUR", EconomyCompat.formatValue(currency, BigInteger.valueOf(1234), false));
    }

    // parseValue()

    @Test
    void parseValueWithoutDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals(123, EconomyCompat.parseValue(currency, "$123").longValueExact());
    }

    @Test
    void parseValueWithDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals(1234, EconomyCompat.parseValue(currency, "$12.34").longValueExact());
        assertEquals(5, EconomyCompat.parseValue(currency, "$0.05").longValueExact());
    }

    @Test
    void parseValuePrefixAndSuffix() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "€", " EUR")
        );

        assertEquals(1234, EconomyCompat.parseValue(currency, "€12.34 EUR").longValueExact());
    }

    @Test
    void parseValueEmptyReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            ResourceId.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals(0, EconomyCompat.parseValue(currency, "").longValueExact());
    }
}
