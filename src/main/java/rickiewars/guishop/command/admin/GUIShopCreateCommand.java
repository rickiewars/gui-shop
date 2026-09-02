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
import rickiewars.guishop.shop.Shop;

public class GUIShopCreateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .then(CommandManager.literal("create")
                    .requires(GuiShopPermission.CREATE_SHOP.require())
                    .then(CommandManager.argument("shopName", StringArgumentType.string())
                        .executes(GUIShopCreateCommand::run))));
    }

    public static int run(CommandContext<ServerCommandSource> context){
        var shopName = StringArgumentType.getString(context, "shopName");

        var existingIds = new java.util.HashSet<String>();
        GUIShop.shops.forEach(s -> existingIds.add(s.getId()));
        String id = rickiewars.guishop.util.CommonMethods.slugify(shopName, existingIds);

        Shop shop = new Shop(id, shopName);
        GUIShop.shops.add(shop);
        GUIShop.shopStore.writeShop(shop);

        context.getSource().sendFeedback(()-> Text.literal("Shop successfully created!").formatted(Formatting.GREEN), false);
        return 0;
    }
}
