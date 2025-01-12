package unsafedodo.guishop.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;
import unsafedodo.guishop.util.CommonMethods;

import java.util.Objects;

public class GUIShopAddHeldItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("addhelditem")
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new CommonMethods.ShopNameSuggestionProvider())
                        .then(CommandManager.argument("itemName", StringArgumentType.string())
                            .then(CommandManager.argument("buyItemPrice", LongArgumentType.longArg(-1))
                                .then(CommandManager.argument("sellItemPrice", LongArgumentType.longArg(-1))
                                    .requires(Permissions.require("guishop.additem", 2))
                                    .executes(GUIShopAddHeldItemCommand::run)))))));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        long buyItemPrice = LongArgumentType.getLong(context, "buyItemPrice");
        long sellItemPrice = LongArgumentType.getLong(context, "sellItemPrice");

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
                new String[]{},
                heldItemComponentChanges
        ));
        context.getSource().sendFeedback(() -> Text.literal("Item successfully added").formatted(Formatting.GREEN), false);

        return 0;
    }
}