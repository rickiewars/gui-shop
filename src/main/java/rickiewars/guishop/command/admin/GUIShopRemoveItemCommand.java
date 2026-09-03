package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopRemoveItemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("removeitem")
                .requires(GuiShopPermission.REMOVE_ITEM.require())
                .then(Commands.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(Commands.argument("itemName", StringArgumentType.string())
                        .executes(GUIShopRemoveItemCommand::run)))));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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
        context.getSource().sendSuccess(()-> Component.literal(
            "Item successfully removed"
        ).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}
