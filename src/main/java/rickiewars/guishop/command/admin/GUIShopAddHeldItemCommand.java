package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.command.suggestions.CurrencySuggestionProvider;
import rickiewars.guishop.command.suggestions.ShopNameSuggestionProvider;
import rickiewars.guishop.economy.economyProvider.GuiShopEconomyCurrency;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

import java.util.Objects;

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
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        long buyItemPrice = LongArgumentType.getLong(context, "buyItemPrice");
        long sellItemPrice = LongArgumentType.getLong(context, "sellItemPrice");

        String currency = GuiShopEconomyCurrency.DEFAULT_ID;
        try {
            currency = StringArgumentType.getString(context, "currency");
        } catch (IllegalArgumentException ignored) {}


        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) {
            context.getSource().sendFeedback(() -> Text.literal(String.format("Shop %s not found", shopName)).formatted(Formatting.RED), false);
            return -1;
        }

        ItemStack heldItem;
        try {
            heldItem = Objects.requireNonNull(context.getSource().getPlayer()).getMainHandStack();
        } catch (NullPointerException npe) {
            context.getSource().sendFeedback(() -> Text.literal("You must can only run this command as a player").formatted(Formatting.RED), false);
            return -1;
        }
        if (heldItem.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You must be holding an item to add it to the shop").formatted(Formatting.RED), false);
            return -1;
        }

        String itemId = Registries.ITEM.getId(heldItem.getItem()).toString();

        ComponentChanges heldItemComponentChanges = heldItem.getComponentChanges();

        foundShop.getItems().add(new ShopItem(
                itemName,
                itemId,
                buyItemPrice,
                sellItemPrice,
                currency,
                new String[]{},
                heldItemComponentChanges
        ));
        context.getSource().sendFeedback(() -> Text.literal("Item successfully added").formatted(Formatting.GREEN), false);

        return 0;
    }
}