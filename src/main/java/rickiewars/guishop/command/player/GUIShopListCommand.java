package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
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

    public static int runAllShops(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(GUIShop.config.shops.isEmpty()) throw CommandErrors.NO_SHOPS_AVAILABLE.create();

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

    }

    public static int runSpecificShop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        Shop foundShop = CommonMethods.getShopByName(shopName);

        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(foundShop.getName()).append(" items list:\n\n");
        for(ShopItem item: foundShop.getItems()){
            stringBuilder.append("Item name: ").append(item.itemName()).append(", ")
                .append("Buy price: ").append(item.formatCurrency(item.buyItemPrice())).append(", ")
                .append("Sell price: ").append(item.formatCurrency(item.sellItemPrice())).append("\n\n");
        }

        String msg = StringUtils.chomp(stringBuilder.toString());
        context.getSource().sendFeedback(()-> Text.literal(msg).formatted(Formatting.AQUA), false);

        return 0;
    }
}
