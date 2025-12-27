package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

public class GUIShopListCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("list")
                .requires(GuiShopPermission.LIST.require())
                .executes(GUIShopListCommand::runAllShops)
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .requires(GuiShopPermission.LIST_ITEMS.require())
                    .executes(GUIShopListCommand::runSpecificShop)
                )));
    }

    public static int runAllShops(CommandContext<ServerCommandSource> context){
        if(!GUIShop.config.shops.isEmpty()){
            StringBuilder msgBldr = new StringBuilder();

            for(Shop shop: GUIShop.config.shops){
                msgBldr.append(shop.getName());
                if (shop.getItems().isEmpty()) {
                    msgBldr.append(" (Out of stock)");
                }
                msgBldr.append("\n");
            }
            String msg = StringUtils.chomp(msgBldr.toString());

            context.getSource().sendFeedback(()-> Text.literal(msg).formatted(Formatting.AQUA), false);

            return 0;
        } else
            context.getSource().sendFeedback(()->Text.literal("No shops available").formatted(Formatting.RED), false);

        return -1;

    }

    public static int runSpecificShop(CommandContext<ServerCommandSource> context){
        String shopName = StringArgumentType.getString(context, "shopName");
        Shop foundShop = CommonMethods.getShopByName(shopName);

        if(foundShop != null){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n").append(foundShop.getName()).append(" items list:\n\n");
            for(ShopItem item: foundShop.getItems()){
                stringBuilder.append("Item name: ").append(item.itemName()).append(", ")
                    .append("Buy price: ").append(item.formatCurrency(item.buyItemPrice())).append(", ")
                    .append("Sell price: ").append(item.formatCurrency(item.sellItemPrice())).append("\n\n");
            }

            String msg = StringUtils.chomp(stringBuilder.toString());
            context.getSource().sendFeedback(()-> Text.literal(msg).formatted(Formatting.AQUA), false);
        } else
            context.getSource().sendFeedback(()->Text.literal("Shop not found!").formatted(Formatting.RED), false);

        return 0;
    }
}
