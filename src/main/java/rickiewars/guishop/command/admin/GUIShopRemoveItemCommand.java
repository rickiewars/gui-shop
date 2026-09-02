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
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopRemoveItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("removeitem")
                .requires(GuiShopPermission.REMOVE_ITEM.require())
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(CommandManager.argument("itemName", StringArgumentType.string())
                        .executes(GUIShopRemoveItemCommand::run)))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        var item = foundShop.getItems().stream().filter(
            i -> i.displayName().equals(itemName)
        ).findFirst();
        if (item.isEmpty()) throw CommandErrors.ITEM_NOT_FOUND.create(itemName);

        foundShop.getItems().remove(item.get());
        rickiewars.guishop.GUIShop.shopStore.writeShop(foundShop);
        context.getSource().sendFeedback(()-> Text.literal(
            "Item successfully removed"
        ).formatted(Formatting.GREEN), false);
        return 0;
    }
}
