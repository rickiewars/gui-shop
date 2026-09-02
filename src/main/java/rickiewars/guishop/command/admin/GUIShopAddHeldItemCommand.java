package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
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
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("addhelditem")
                .requires(GuiShopPermission.ADD_ITEM.require())
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(CommandManager.argument("itemName", StringArgumentType.string())
                        .then(CommandManager.argument("buyItemPrice", LongArgumentType.longArg(-1))
                            .then(CommandManager.argument("sellItemPrice", LongArgumentType.longArg(-1))
                                .executes(GUIShopAddHeldItemCommand::run)
                                .then(CommandManager.argument("currency", IdentifierArgumentType.identifier())
                                    .suggests(new CurrencySuggestionProvider())
                                    .executes(GUIShopAddHeldItemCommand::run)
                                )))))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        long buyItemPrice = LongArgumentType.getLong(context, "buyItemPrice");
        long sellItemPrice = LongArgumentType.getLong(context, "sellItemPrice");

        Identifier currency = GuiShopEconomyCurrency.DEFAULT_ID;
        try {
            currency = IdentifierArgumentType.getIdentifier(context, "currency");
        } catch (IllegalArgumentException ignored) {}

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) throw CommandErrors.SHOP_NOT_FOUND.create(shopName);

        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty()) throw CommandErrors.HAND_EMPTY.create();

        foundShop.getItems().add(new ShopItem(
                itemName,
                new MinecraftItemStack(heldItem.copyWithCount(1)),
                buyItemPrice,
                sellItemPrice,
                currency,
                List.of()
        ));
        GUIShop.shopStore.writeShop(foundShop);
        context.getSource().sendFeedback(() -> Text.literal("Item successfully added").formatted(Formatting.GREEN), false);
        return 0;
    }
}