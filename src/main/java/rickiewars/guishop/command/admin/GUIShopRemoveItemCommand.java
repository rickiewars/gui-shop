package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopRemoveItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("removeitem")
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(CommandManager.argument("itemName", StringArgumentType.string())
                        .requires(GuiShopPermission.REMOVE_ITEM.require())
                        .executes(GUIShopRemoveItemCommand::run)))));
    }

    public static int run(CommandContext<ServerCommandSource> context){
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        ShopItem foundItem = null;

        Shop foundShop = CommonMethods.getShopByName(shopName);

        if(foundShop != null){
            for(ShopItem item: foundShop.getItems()){
                if(item.itemName().equals(itemName)){
                    foundItem = item;
                    break;
                }
            }

            if(foundItem != null){
                foundShop.getItems().remove(foundItem);
                context.getSource().sendFeedback(()-> Text.literal("Item successfully removed").formatted(Formatting.GREEN), false);
            } else
                context.getSource().sendFeedback(()-> Text.literal("Item not found").formatted(Formatting.RED), false);

        } else
            context.getSource().sendFeedback(()-> Text.literal("Shop not found").formatted(Formatting.RED), false);


        return 0;
    }
}
