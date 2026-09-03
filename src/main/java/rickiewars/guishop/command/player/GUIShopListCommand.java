package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopListCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("list")
                .requires(GuiShopPermission.LIST.require())
                .executes(GUIShopListCommand::runAllShops)
                .then(Commands.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .requires(GuiShopPermission.LIST_ITEMS.require())
                    .executes(GUIShopListCommand::runSpecificShop)
                )));
    }

    public static int runAllShops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(GUIShop.shops.isEmpty()) throw CommandErrors.NO_SHOPS_AVAILABLE.create();

        StringBuilder msgBldr = new StringBuilder();
        for(Shop shop: GUIShop.shops){
            msgBldr.append(shop.getDisplayName());
            if (shop.getItems().isEmpty()) {
                msgBldr.append(" (Out of stock)");
            }
            msgBldr.append("\n");
        }
        String msg = StringUtils.chomp(msgBldr.toString());

        context.getSource().sendSuccess(()-> Component.literal(msg).withStyle(ChatFormatting.AQUA), false);

        return 0;

    }

    public static int runSpecificShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        Shop foundShop = CommonMethods.getShopByName(shopName);

        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(foundShop.getDisplayName()).append(" items list:\n\n");
        for(ShopItem item: foundShop.getItems()){
            stringBuilder.append("Item name: ").append(item.displayName()).append(", ")
                .append("Buy price: ").append(item.formatCurrency(item.buyPrice())).append(", ")
                .append("Sell price: ").append(item.formatCurrency(item.sellPrice())).append("\n\n");
        }

        String msg = StringUtils.chomp(stringBuilder.toString());
        context.getSource().sendSuccess(()-> Component.literal(msg).withStyle(ChatFormatting.AQUA), false);

        return 0;
    }
}
