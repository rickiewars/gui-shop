package unsafedodo.guishop.command;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;
import unsafedodo.guishop.util.CommonMethods;

import java.util.Optional;

public class GUIShopAddItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("additem")
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new CommonMethods.ShopNameSuggestionProvider())
                        .then(CommandManager.argument("itemName", StringArgumentType.string())
                            .then(CommandManager.argument("itemId", StringArgumentType.string())
                                .then(CommandManager.argument("buyItemPrice", FloatArgumentType.floatArg(-1.0f))
                                    .then(CommandManager.argument("sellItemPrice", FloatArgumentType.floatArg(-1.0f))
                                        .then(CommandManager.argument("description", StringArgumentType.string())
                                            .then(CommandManager.argument("componentChanges", StringArgumentType.string())
                                                .requires(Permissions.require("guishop.additem", 2))
                                                .executes(GUIShopAddItemCommand::run))))))))));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        String itemId = StringArgumentType.getString(context, "itemId");
        float buyItemPrice = FloatArgumentType.getFloat(context, "buyItemPrice");
        float sellItemPrice = FloatArgumentType.getFloat(context, "sellItemPrice");
        String descriptionLine = StringArgumentType.getString(context, "description");
        String componentChangesString = StringArgumentType.getString(context, "componentChanges");

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) {
            context.getSource().sendFeedback(() -> Text.literal(String.format("Shop %s not found", shopName)).formatted(Formatting.RED), false);
            return -1;
        }

        Optional<Item> item = Registries.ITEM.getOrEmpty(new Identifier(itemId));
        if (item.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal(
                    "Unknown item id \"" + itemId + "\""
            ).formatted(Formatting.RED), false);
            return -1;
        }
        String registryItemId = Registries.ITEM.getId(item.get()).toString();

        String[] description = descriptionLine.split("\\\\");

        ComponentChanges componentChanges;
        try {
            JsonElement jsonInput = JsonParser.parseString(
                    componentChangesString.isEmpty() ? "{}" : componentChangesString
            );
            componentChanges = ComponentChanges.CODEC.parse(
                    JsonOps.INSTANCE, jsonInput
            ).getOrThrow();
        } catch (JsonSyntaxException e) {
            context.getSource().sendFeedback(() -> Text.literal("Error parsing json component").formatted(Formatting.RED), false);
            String message = CommonMethods.findRootCause(e).getLocalizedMessage();
            context.getSource().sendFeedback(() -> Text.literal(message).formatted(Formatting.RED), false);
            return -1;
        } catch (Exception e){
            context.getSource().sendFeedback(()-> Text.literal("Error parsing component changes").formatted(Formatting.RED), false);
            String message = CommonMethods.findRootCause(e).getLocalizedMessage();
            context.getSource().sendFeedback(() -> Text.literal(message).formatted(Formatting.RED), false);
            return -1;
        }

        foundShop.getItems().add(new ShopItem(itemName, registryItemId, buyItemPrice, sellItemPrice, description, componentChanges));
        context.getSource().sendFeedback(() -> Text.literal("Item successfully added").formatted(Formatting.GREEN), false);

        return 0;
    }
}
