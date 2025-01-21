package rickiewars.guishop.command.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
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
import rickiewars.guishop.util.ServerHandler;

public class GUIShopAddItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("additem")
                .requires(GuiShopPermission.ADD_ITEM.require())
                .then(CommandManager.argument("shopName", StringArgumentType.string())
                    .suggests(new ShopNameSuggestionProvider())
                    .then(CommandManager.argument("itemName", StringArgumentType.string())
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                            .then(CommandManager.argument("buyItemPrice", LongArgumentType.longArg(-1))
                                .then(CommandManager.argument("sellItemPrice", LongArgumentType.longArg(-1))
                                    .then(CommandManager.argument("currency", IdentifierArgumentType.identifier())
                                        .suggests(new CurrencySuggestionProvider())
                                        .executes(GUIShopAddItemCommand::run)
                                        .then(CommandManager.argument("description", StringArgumentType.string())
                                            .executes(GUIShopAddItemCommand::run)
                                            .then(CommandManager.argument("componentChanges", StringArgumentType.string())
                                                .executes(GUIShopAddItemCommand::run)
                                            ))))))))));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        String shopName = StringArgumentType.getString(context, "shopName");
        String itemName = StringArgumentType.getString(context, "itemName");
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        long buyItemPrice = LongArgumentType.getLong(context, "buyItemPrice");
        long sellItemPrice = LongArgumentType.getLong(context, "sellItemPrice");

        String currency = GuiShopEconomyCurrency.DEFAULT_ID;
        try {
            currency = IdentifierArgumentType.getIdentifier(context, "currency").toString();
        } catch (IllegalArgumentException ignored) {}

        String descriptionLine = "";
        try {
            descriptionLine = StringArgumentType.getString(context, "currency");
        } catch (IllegalArgumentException ignored) {}

        String componentChangesString = "";
        try {
            componentChangesString = StringArgumentType.getString(context, "currency");
        } catch (IllegalArgumentException ignored) {}

        Shop foundShop = CommonMethods.getShopByName(shopName);
        if (foundShop == null) {
            context.getSource().sendFeedback(() -> Text.literal(String.format("Shop %s not found", shopName)).formatted(Formatting.RED), false);
            return -1;
        }

        String registryItemId = Registries.ITEM.getId(item).toString();

        String[] description = descriptionLine.split("\\\\");

        ComponentChanges componentChanges;
        try {
            JsonElement jsonInput = JsonParser.parseString(
                    componentChangesString.isEmpty() ? "{}" : componentChangesString
            );
            componentChanges = ComponentChanges.CODEC.parse(
                    ServerHandler.getRegistryManager().getOps(JsonOps.INSTANCE), jsonInput
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

        foundShop.getItems().add(new ShopItem(itemName, registryItemId, buyItemPrice, sellItemPrice, currency, description, componentChanges));
        context.getSource().sendFeedback(() -> Text.literal("Item successfully added").formatted(Formatting.GREEN), false);

        return 0;
    }
}
