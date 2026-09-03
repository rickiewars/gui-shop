package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;

public class GUIShopCreateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
                .then(Commands.literal("create")
                    .requires(GuiShopPermission.CREATE_SHOP.require())
                    .then(Commands.argument("shopName", StringArgumentType.string())
                        .executes(GUIShopCreateCommand::run))));
    }

    public static int run(CommandContext<CommandSourceStack> context){
        var shopName = StringArgumentType.getString(context, "shopName");

        var existingIds = new java.util.HashSet<String>();
        GUIShop.shops.forEach(s -> existingIds.add(s.getId()));
        String id = rickiewars.guishop.util.CommonMethods.slugify(shopName, existingIds);

        Shop shop = new Shop(id, shopName);
        GUIShop.shops.add(shop);
        GUIShop.shopStore.writeShop(shop);

        context.getSource().sendSuccess(()-> Component.literal("Shop successfully created!").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}
