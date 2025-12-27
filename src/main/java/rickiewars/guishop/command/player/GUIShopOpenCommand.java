package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopOpenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .then(CommandManager.literal("open")
                    .requires(GuiShopPermission.OPEN.require())
                    .then(CommandManager.argument("shopName", StringArgumentType.string())
                        .suggests(new ShopNameSuggestionProvider())
                        .executes(GUIShopOpenCommand::run)
                        .then(CommandManager.argument("playerName", EntityArgumentType.player())
                            .requires(GuiShopPermission.OPEN_FOR_PLAYER.require())
                            .executes(GUIShopOpenCommand::run)))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        Shop selectedShop = CommonMethods.getShopByName(shopName);

        if(selectedShop != null){
            if(!selectedShop.getItems().isEmpty()){
                ServerPlayerEntity player;
                try {
                    player = EntityArgumentType.getPlayer(context, "playerName");
                } catch (Exception _e) {
                    player = context.getSource().getPlayer();
                }

                if (player == null) {
                    context.getSource().sendFeedback(() -> Text.literal(
                        "If the command is run from the console, you must specify a player!"
                    ).formatted(Formatting.RED), false);
                    return -1;
                }

                var menu = new ShopMenu(selectedShop, new MinecraftPlayer(player));
                var controller = new MinecraftMenuController(player);
                return controller.open(menu) ? 0 : -1;
            }else{
                context.getSource().sendFeedback(()->Text.literal("The shop does not contain any items").formatted(Formatting.RED), false);
                return -1;
            }

        }else
            context.getSource().sendFeedback(()-> Text.literal("Shop not found").formatted(Formatting.RED), false);
        return 0;
    }
}
