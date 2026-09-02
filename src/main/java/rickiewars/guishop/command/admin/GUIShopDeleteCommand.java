package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;

public class GUIShopDeleteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .then(CommandManager.literal("delete")
                        .requires(GuiShopPermission.DELETE_SHOP.require())
                        .then(CommandManager.argument("shopName", StringArgumentType.string())
                            .suggests(new ShopNameSuggestionProvider())
                            .executes(GUIShopDeleteCommand::run))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");

        var shop = GUIShop.shops.stream().filter(
            match -> match.getDisplayName().equals(shopName)
        ).findFirst();
        if (shop.isEmpty()) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        GUIShop.shops.remove(shop.get());
        GUIShop.shopStore.deleteShop(shop.get());
        context.getSource().sendFeedback(()-> Text.literal(
            "Shop successfully removed!"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}
