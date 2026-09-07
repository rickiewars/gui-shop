package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.CurrencySuggestionProvider;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

import java.util.List;

public class GUIShopAddHeldItemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("addhelditem")
                .requires(GuiShopPermission.ADD_ITEM.require())
                .then(Commands.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(Commands.argument("itemName", StringArgumentType.string())
                        .then(Commands.argument("buyItemPrice", LongArgumentType.longArg())
                            .then(Commands.argument("sellItemPrice", LongArgumentType.longArg())
                                .executes(GUIShopAddHeldItemCommand::run)
                                .then(Commands.argument("currency", IdentifierArgument.id())
                                    .suggests(new CurrencySuggestionProvider())
                                    .executes(GUIShopAddHeldItemCommand::run)
                                    .then(Commands.argument("description", StringArgumentType.string())
                                        .executes(GUIShopAddHeldItemCommand::run)
                                    ))))))));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        long buyItemPrice = normalizePrice(LongArgumentType.getLong(context, "buyItemPrice"));
        long sellItemPrice = normalizePrice(LongArgumentType.getLong(context, "sellItemPrice"));

        Identifier currency = null;
        try {
            currency = IdentifierArgument.getId(context, "currency");
        } catch (IllegalArgumentException ignored) {}

        String descriptionLine = "";
        try {
            descriptionLine = StringArgumentType.getString(context, "description");
        } catch (IllegalArgumentException ignored) {}

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) throw CommandErrors.HAND_EMPTY.create();

        if (buyItemPrice == -1 && sellItemPrice == -1) throw CommandErrors.BUY_AND_SELL_BOTH_DISABLED.create();

        List<String> description = descriptionLine.isEmpty() ? List.of() : List.of(descriptionLine.split("\\\\"));

        foundShop.getItems().add(new ShopItem(
                itemName,
                new MinecraftItemStack(heldItem.copyWithCount(1)),
                buyItemPrice,
                sellItemPrice,
                currency,
                description
        ));
        GUIShop.shopStore.writeShop(foundShop);
        context.getSource().sendSuccess(() -> Component.literal("Item successfully added").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static long normalizePrice(long price) {
        return price < 0 ? -1 : price;
    }
}