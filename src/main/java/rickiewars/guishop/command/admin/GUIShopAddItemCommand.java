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
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.CurrencySuggestionProvider;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

import java.util.List;

public class GUIShopAddItemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("additem")
                .requires(GuiShopPermission.ADD_ITEM.require())
                .then(Commands.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(Commands.argument("itemName", StringArgumentType.string())
                        .then(Commands.argument("item", ItemArgument.item(commandRegistryAccess))
                            .then(Commands.argument("buyItemPrice", LongArgumentType.longArg())
                                .then(Commands.argument("sellItemPrice", LongArgumentType.longArg())
                                    .executes(GUIShopAddItemCommand::run)
                                    .then(Commands.argument("currency", IdentifierArgument.id())
                                        .suggests(new CurrencySuggestionProvider())
                                        .executes(GUIShopAddItemCommand::run)
                                        .then(Commands.argument("description", StringArgumentType.string())
                                            .executes(GUIShopAddItemCommand::run)
                                        )))))))));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var itemStackArgument = ItemArgument.getItem(context, "item");
        var itemStack = itemStackArgument.createItemStack(1, false);

        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        long buyItemPrice = normalizePrice(LongArgumentType.getLong(context, "buyItemPrice"));
        long sellItemPrice = normalizePrice(LongArgumentType.getLong(context, "sellItemPrice"));

        ResourceId currency = null;
        try {
            Identifier rawCurrency = IdentifierArgument.getId(context, "currency");
            currency = ResourceId.of(rawCurrency.getNamespace(), rawCurrency.getPath());
        } catch (IllegalArgumentException ignored) {}

        String descriptionLine = "";
        try {
            descriptionLine = StringArgumentType.getString(context, "description");
        } catch (IllegalArgumentException ignored) {}

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        if (buyItemPrice == -1 && sellItemPrice == -1) throw CommandErrors.BUY_AND_SELL_BOTH_DISABLED.create();

        List<String> description = descriptionLine.isEmpty() ? List.of() : List.of(descriptionLine.split("\\\\"));

        foundShop.getItems().add(new ShopItem(itemName, new MinecraftItemStack(itemStack.copyWithCount(1)), buyItemPrice, sellItemPrice, currency, description));
        GUIShop.shopStore.writeShop(foundShop);
        context.getSource().sendSuccess(() -> Component.literal("Item successfully added").withStyle(ChatFormatting.GREEN), false);

        return 0;
    }

    private static long normalizePrice(long price) {
        return price < 0 ? -1 : price;
    }
}
