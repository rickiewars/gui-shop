package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.command.GuiShopPermission;

import java.net.URI;

public class GUIShopMainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .requires(GuiShopPermission.MAIN.require())
                .then(CommandManager.literal("help")
                        .requires(GuiShopPermission.HELP.require())
                        .executes(GUIShopMainCommand::runHelp))
                .executes(GUIShopMainCommand::run));
    }

    private static int runHelp(CommandContext<ServerCommandSource> context) {
        String msg = """
                /shop additem <shopName> <itemName> <itemId> <buyPrice> <sellPrice> [Description] [componentData] [qty1:qt2:...:qt5]
                /shop removeitem <shopName> <itemName>
                /shop create <shopName>
                /shop delete <shopName>
                /shop list
                /shop list [shopName]
                /shop open <shopName>
                /shop forcesave
                /shop reload""";

        context.getSource().sendFeedback(()->Text.literal(msg).formatted(Formatting.YELLOW), false);

        return 0;
    }

    public static int run(CommandContext<ServerCommandSource> context){
        String git = "https://github.com/rickiewars/gui-shop";
        context.getSource().sendFeedback(()-> Text.literal("""
            GUIShop by Rickiewars is running!
            This project is a fork of the original work by UnsafeDodo.
            Check the GitHub repository for usage:
            """).formatted(Formatting.GREEN), false);
        context.getSource().sendFeedback(()->Text.literal(git).setStyle(Style.EMPTY.withUnderline(true)).styled(
            style -> {
                try {
                    return style.withClickEvent(new ClickEvent.OpenUrl(new URI(git)));
                } catch (Exception e) {
                    return style.withClickEvent(new ClickEvent.CopyToClipboard(git));
                }
            }
        ).formatted(Formatting.BLUE), false);
        return 0;
    }
}
