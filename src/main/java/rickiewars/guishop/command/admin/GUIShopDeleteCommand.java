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
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;

public class GUIShopDeleteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
                .then(Commands.literal("delete")
                        .requires(GuiShopPermission.DELETE_SHOP.require())
                        .then(Commands.argument("shopName", StringArgumentType.string())
                            .suggests(new ShopNameSuggestionProvider())
                            .executes(GUIShopDeleteCommand::run))));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");

        var shop = GUIShop.shops.stream().filter(
            match -> match.getDisplayName().equals(shopName)
        ).findFirst();
        if (shop.isEmpty()) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        GUIShop.shops.remove(shop.get());
        GUIShop.shopStore.deleteShop(shop.get());
        context.getSource().sendSuccess(()-> Component.literal(
            "Shop successfully removed!"
        ).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}
