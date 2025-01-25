package rickiewars.guishop.command.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.CommonMethods;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShopNameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        List<Shop> shops = CommonMethods.getAllShops();
        for (Shop shop : shops) {
            if (shop.getName().toLowerCase().startsWith(input)) {
                builder.suggest(shop.getName());
            }
        }
        return builder.buildFuture();
    }
}
