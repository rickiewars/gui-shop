package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
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
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
                .then(Commands.literal("open")
                    .requires(GuiShopPermission.OPEN.require())
                    .then(Commands.argument("shopName", StringArgumentType.string())
                        .suggests(new ShopNameSuggestionProvider())
                        .executes(GUIShopOpenCommand::run)
                        .then(Commands.argument("playerName", EntityArgument.player())
                            .requires(GuiShopPermission.OPEN_FOR_PLAYER.require())
                            .executes(GUIShopOpenCommand::run)))));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        Shop selectedShop = CommonMethods.getShopByName(shopName);
        ServerPlayer player = getPlayer(context)
            .orElse(context.getSource().getPlayer());

        if (player == null) throw CommandErrors.NEED_PLAYER.create();
        if(selectedShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);
        if(selectedShop.getItems().isEmpty()) throw CommandErrors.SHOP_NOT_AVAILABLE.create(shopName);

        var menu = new ShopMenu(selectedShop, new MinecraftPlayer(player));
        var controller = new MinecraftMenuController(player);
        return controller.open(menu) ? 0 : -1;
    }

    private static Optional<ServerPlayer> getPlayer(CommandContext<CommandSourceStack> context) {
        try {
            return Optional.ofNullable(EntityArgument.getPlayer(context, "playerName"));
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            return Optional.empty();
        }
    }
}
