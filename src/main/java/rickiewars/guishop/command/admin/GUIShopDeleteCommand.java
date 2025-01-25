package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.shop.Shop;

public class GUIShopDeleteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .then(CommandManager.literal("delete")
                        .requires(GuiShopPermission.DELETE_SHOP.require())
                        .then(CommandManager.argument("shopName", StringArgumentType.string())
                            .suggests(new ShopNameSuggestionProvider())
                            .executes(GUIShopDeleteCommand::run))));
    }

    public static int run(CommandContext<ServerCommandSource> context){
        String shopName = StringArgumentType.getString(context, "shopName");
        boolean found = false;

        for(Shop shop: GUIShop.config.shops){
            if(shop.getName().equals(shopName)){
                GUIShop.config.shops.remove(shop);
                found = true;
                break;
            }
        }

        if(found)
            context.getSource().sendFeedback(()-> Text.literal("Shop successfully removed!").formatted(Formatting.GREEN), false);
        else
            context.getSource().sendFeedback(()-> Text.literal("Shop not found!").formatted(Formatting.RED), false);

        return 0;
    }
}
