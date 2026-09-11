package rickiewars.guishop.command.suggestions;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import rickiewars.guishop.command.CommandTestBase;

import java.util.List;

/**
 * Base for {@code SuggestionProvider} game tests: parses a partial command as an operator-level
 * console source and asks the dispatcher for completions at the end of it, exactly like a
 * client's tab-complete.
 */
abstract class SuggestionsCommandTest extends CommandTestBase {

    static List<String> suggestionsFor(GameTestHelper context, String partialCommand) {
        MinecraftServer server = context.getLevel().getServer();
        CommandSourceStack source = withAllPermissions(server.createCommandSourceStack());

        var dispatcher = server.getCommands().getDispatcher();
        ParseResults<CommandSourceStack> parsed = dispatcher.parse(partialCommand, source);

        return dispatcher.getCompletionSuggestions(parsed).join().getList().stream()
            .map(Suggestion::getText)
            .toList();
    }
}
