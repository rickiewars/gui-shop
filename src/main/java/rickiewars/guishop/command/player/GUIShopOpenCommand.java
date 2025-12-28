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
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.util.CommonMethods;

import java.util.Optional;

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
        ServerPlayerEntity player = getPlayer(context)
            .orElse(context.getSource().getPlayer());

        if (player == null) throw CommandErrors.NEED_PLAYER.create();
        if(selectedShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);
        if(selectedShop.getItems().isEmpty()) throw CommandErrors.SHOP_NOT_AVAILABLE.create(shopName);

        var menu = new ShopMenu(selectedShop, new MinecraftPlayer(player));
        var controller = new MinecraftMenuController(player);
        return controller.open(menu) ? 0 : -1;
    }

    private static Optional<ServerPlayerEntity> getPlayer(CommandContext<ServerCommandSource> context) {
        try {
            return Optional.ofNullable(EntityArgumentType.getPlayer(context, "playerName"));
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            return Optional.empty();
        }
    }
}
