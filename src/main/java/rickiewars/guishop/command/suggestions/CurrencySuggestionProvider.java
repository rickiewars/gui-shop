package rickiewars.guishop.command.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.concurrent.CompletableFuture;

public class CurrencySuggestionProvider  implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();
        String input = builder.getRemaining().toLowerCase();

        EconomyUtils.getCurrencies(server).forEach(currency -> {
            if (currency.id().toString().toLowerCase().startsWith(input)
                    || currency.id().getPath().toLowerCase().startsWith(input)
                    || currency.name().getString().toLowerCase().startsWith(input)
            ){
                builder.suggest(currency.id().toString(), currency.name());
            }
        });
        return builder.buildFuture();
    }
}
