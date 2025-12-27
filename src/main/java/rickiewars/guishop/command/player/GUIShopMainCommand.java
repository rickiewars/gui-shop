package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.gui.SelectShopMenu;

import java.net.URI;
import java.util.function.UnaryOperator;

public class GUIShopMainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        var root = dispatcher.getRoot();
        boolean shopExists = root.getChildren().stream().anyMatch(
            node -> node.getName().equals("shop")
        );
        boolean buyExists = root.getChildren().stream().anyMatch(
            node -> node.getName().equals("buy")
        );
        boolean tradeExists = root.getChildren().stream().anyMatch(
            node -> node.getName().equals("buy")
        );

        dispatcher.register(buildTree("guishop"));
        if (!shopExists) dispatcher.register(buildTree("shop"));
        if (!buyExists) dispatcher.register(buildTree("buy"));
        if (!tradeExists) dispatcher.register(buildTree("trade"));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildTree(String literal) {
        return CommandManager.literal(literal)
            .requires(GuiShopPermission.MAIN.require())
            .executes(GUIShopMainCommand::run)
            .then(CommandManager.literal("help")
                .requires(GuiShopPermission.HELP.require())
                .executes(GUIShopMainCommand::runHelp))
            .executes(GUIShopMainCommand::run);
    }

    private static int runHelp(CommandContext<ServerCommandSource> context) {
        String aboutMsg = """
            GUIShop by Rickiewars is running!
            This project is a fork of the original work by UnsafeDodo.
            Check the GitHub repository for usage:
            """;
        String git = "https://github.com/rickiewars/gui-shop";
        String help = """
            /shop additem <shopName> <itemName> <itemId> <buyPrice> <sellPrice> [Description] [componentData] [qty1:qt2:...:qt5]
            /shop removeitem <shopName> <itemName>
            /shop create <shopName>
            /shop delete <shopName>
            /shop list
            /shop list [shopName]
            /shop open <shopName>
            /shop forcesave
            /shop reload""";

        UnaryOperator<Style> uriStyle = (style) -> {
            try {
                return style.withClickEvent(new ClickEvent.OpenUrl(new URI(git)));
            } catch (Exception e) {
                return style.withClickEvent(new ClickEvent.CopyToClipboard(git));
            }
        };

        context.getSource().sendFeedback(
            () -> Text.literal(aboutMsg).formatted(Formatting.GREEN).append("\n").append(
                Text.literal(git).setStyle(Style.EMPTY.withUnderline(true)).styled(uriStyle).formatted(Formatting.BLUE)
            ).append(Text.literal(help).formatted(Formatting.WHITE)),
            false);

        return 0;
    }

    public static int run(CommandContext<ServerCommandSource> context){
        var player = context.getSource().getPlayer();
        if (player == null) return runHelp(context);

        var shops = GUIShop.config.shops.stream().filter(
            (shop) -> !shop.getItems().isEmpty()
        ).toList();

        if (shops.isEmpty()) {
            context.getSource().sendFeedback(
                () -> Text.literal(
                    "There are no shops available at the moment. Please contact an administrator."
                ).formatted(Formatting.RED),
                false
            );
            return -1;
        }

        Menu menu = new SelectShopMenu(shops, new MinecraftPlayer(player));
        MenuController controller = new MinecraftMenuController(player);

        var success = controller.open(menu);

        return success ? 0 : -1;
    }
}
