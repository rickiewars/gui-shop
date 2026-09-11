package rickiewars.guishop.command.suggestions;


import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.shop.Shop;

public class ShopNameSuggestionProviderTest extends SuggestionsCommandTest {

    @GameTest
    public void suggestsShopNamesMatchingThePrefixCaseInsensitively(GameTestHelper context) {
        GUIShop.shops.add(new Shop("cool_shop", "Cool Shop"));
        GUIShop.shops.add(new Shop("other_shop", "Other Shop"));

        var suggestions = suggestionsFor(context, "guishop list COO");

        context.assertTrue(suggestions.contains("Cool Shop"), Component.literal("expected the matching shop to be suggested"));
        context.assertTrue(!suggestions.contains("Other Shop"), Component.literal("the non-matching shop must not be suggested"));
        context.succeed();
    }

    @GameTest
    public void suggestsNothingWhenNoShopNameMatches(GameTestHelper context) {
        GUIShop.shops.add(new Shop("cool_shop", "Cool Shop"));

        var suggestions = suggestionsFor(context, "guishop list zzz");

        context.assertTrue(suggestions.isEmpty(), Component.literal("expected no suggestions for a non-matching prefix"));
        context.succeed();
    }
}
