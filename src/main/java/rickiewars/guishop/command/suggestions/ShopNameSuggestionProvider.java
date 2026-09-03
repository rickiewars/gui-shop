package rickiewars.guishop.command.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.shop.Shop;

import java.util.concurrent.CompletableFuture;

public class ShopNameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (Shop shop : GUIShop.shops) {
            if (shop.getDisplayName().toLowerCase().startsWith(input)) {
                builder.suggest(shop.getDisplayName(), Component.nullToEmpty(
                    shop.getItems().size() + " items (" + shop.getDefaultCurrencyId() + ")"
                ));
            }
        }
        return builder.buildFuture();
    }
}
