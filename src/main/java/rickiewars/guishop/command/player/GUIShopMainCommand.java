package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftCompat;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.gui.SelectShopMenu;

import java.util.function.UnaryOperator;

public class GUIShopMainCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
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

    private static LiteralArgumentBuilder<CommandSourceStack> buildTree(String literal) {
        return Commands.literal(literal)
            .requires(GuiShopPermission.MAIN.require())
            .executes(GUIShopMainCommand::run)
            .then(Commands.literal("help")
                .requires(GuiShopPermission.HELP.require())
                .executes(GUIShopMainCommand::runHelp))
            .executes(GUIShopMainCommand::run);
    }

    private static int runHelp(CommandContext<CommandSourceStack> context) {
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

        UnaryOperator<Style> uriStyle = (style) -> style.withClickEvent(MinecraftCompat.clickEventOpenUrl(git));

        context.getSource().sendSuccess(
            () -> Component.literal(aboutMsg).withStyle(ChatFormatting.GREEN).append("\n").append(
                Component.literal(git).setStyle(Style.EMPTY.withUnderlined(true)).withStyle(uriStyle).withStyle(ChatFormatting.BLUE)
            ).append(Component.literal(help).withStyle(ChatFormatting.WHITE)),
            false);

        return 0;
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        if (player == null) return runHelp(context);

        var shops = GUIShop.shops.stream().filter(
            (shop) -> !shop.getItems().isEmpty()
        ).toList();

        if (shops.isEmpty()) throw CommandErrors.NO_SHOPS_AVAILABLE.create();

        Menu menu = new SelectShopMenu(shops, new MinecraftPlayer(player));
        MenuController controller = new MinecraftMenuController(player);

        var success = controller.open(menu);

        return success ? 0 : -1;
    }
}
