package rickiewars.guishop.command.suggestions;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class CurrencySuggestionProviderTest extends SuggestionsCommandTest {

    @GameTest
    public void suggestsConfiguredCurrencies(GameTestHelper context) {
        var suggestions = suggestionsFor(context, "guishop balance ");

        context.assertTrue(suggestions.contains("guishop:credit"), "expected the built-in credit currency to be suggested");
        context.succeed();
    }

    @GameTest
    public void suggestsNothingWhenNoCurrencyMatches(GameTestHelper context) {
        var suggestions = suggestionsFor(context, "guishop balance zzz");

        context.assertTrue(suggestions.isEmpty(), "expected no suggestions for a non-matching prefix");
        context.succeed();
    }
}
