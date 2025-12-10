package rickiewars.guishop.api.economy;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.config.EconomyConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class GuiShopEconomyCurrencyTest {

    private EconomyConfig.CurrencyDefinition def(int decimals, String prefix, String suffix) {
        return new EconomyConfig.CurrencyDefinition(
            "Coins",
            prefix,
            suffix,
            decimals,
            new ItemStack(Items.GOLD_NUGGET)
        );
    }

    @Test
    void idReturnsConstructorValue() {
        Identifier id = Identifier.of("test", "gold");
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(id, def(0, "", ""));

        assertEquals(id, currency.id());
    }

    @Test
    void nameReturnsDefinitionName() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(0, "", "")
        );

        assertEquals("Coins", currency.name().getString());
    }

    @Test
    void iconReturnsDefinitionIcon() {
        EconomyConfig.CurrencyDefinition d = def(0, "$", "");
        ItemStack stack = new ItemStack(Items.GOLD_NUGGET);
        d.icon = stack;

        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("x", "y"),
            d
        );

        assertEquals(stack, currency.icon());
    }

    @Test
    void providerReturnsGuiShopEconomyProviderInstance() {
        EconomyConfig.CurrencyDefinition d = def(0, "", "");

        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "coins"),
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
            Identifier.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals("$123", currency.formatValue(123, false));
    }

    @Test
    void formatNegativeValueReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals("$0", currency.formatValue(-10, false));
    }

    @Test
    void formatValueWithDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals("$123.45", currency.formatValue(12345, false));
        assertEquals("$0.05", currency.formatValue(5, false));
    }

    @Test
    void formatNegativeValueWithDecimalsReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals("$0.00", currency.formatValue(-12345, false));
    }

    @Test
    void formatValueWithPrefixAndSuffix() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "€", " EUR")
        );

        assertEquals("€12.34 EUR", currency.formatValue(1234, false));
    }

    // parseValue()

    @Test
    void parseValueWithoutDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(0, "$", "")
        );

        assertEquals(123, currency.parseValue("$123"));
    }

    @Test
    void parseValueWithDecimals() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals(1234, currency.parseValue("$12.34"));
        assertEquals(5, currency.parseValue("$0.05"));
    }

    @Test
    void parseValuePrefixAndSuffix() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "€", " EUR")
        );

        assertEquals(1234, currency.parseValue("€12.34 EUR"));
    }

    @Test
    void parseValueEmptyReturnsZero() {
        GuiShopEconomyCurrency currency = new GuiShopEconomyCurrency(
            Identifier.of("test", "c"),
            def(2, "$", "")
        );

        assertEquals(0, currency.parseValue(""));
    }
}
