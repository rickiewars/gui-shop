package rickiewars.guishop.errors;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CommandErrors {
    private CommandErrors() {}

    public static final SimpleCommandExceptionType NEED_PLAYER =
        new SimpleCommandExceptionType(Component.literal(
            "If the command is run from the console, you must specify a player!"
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType TARGET_SELF =
        new SimpleCommandExceptionType(Component.literal(
            "You cannot target yourself!"
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType SHOP_NOT_FOUND =
        new DynamicCommandExceptionType(shop -> Component.literal(
            "Shop '" + shop + "' does not exist."
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType NO_SHOPS_AVAILABLE =
        new SimpleCommandExceptionType(Component.literal(
            "There are no shops available at the moment. Please contact an administrator."
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType SHOP_NOT_AVAILABLE =
        new DynamicCommandExceptionType(shop -> Component.literal(
            "Shop '" + shop + "' is not available."
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType HAND_EMPTY =
        new SimpleCommandExceptionType(Component.literal(
            "You must be holding an item in your main hand to do that."
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType ITEM_NOT_FOUND =
        new DynamicCommandExceptionType(item -> Component.literal(
            "Item '" + item + "' was not found in this shop."
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType ITEM_NOT_SELLABLE =
        new SimpleCommandExceptionType(Component.literal(
            "The item you are holding is not sellable in this shop."
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType NO_CURRENCIES =
        new SimpleCommandExceptionType(Component.literal(
            "There are no currencies available at the moment. Please contact an administrator."
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType CURRENCY_NOT_FOUND =
        new DynamicCommandExceptionType(currency -> Component.literal(
            "Currency '" + currency + "' does not exist."
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType ACCOUNT_NOT_FOUND =
        new DynamicCommandExceptionType(player -> Component.literal(
            "Account for player '" + player + "' was not found."
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType AMOUNT_MUST_BE_POSITIVE =
        new SimpleCommandExceptionType(Component.literal(
            "The amount must be greater than 0."
        ).withStyle(ChatFormatting.RED));

    public static final DynamicCommandExceptionType TRANSACTION_FAILED =
        new DynamicCommandExceptionType(reason -> Component.literal(
            "Transaction failed: " + reason
        ).withStyle(ChatFormatting.RED));

    public static final SimpleCommandExceptionType BUY_AND_SELL_BOTH_DISABLED =
        new SimpleCommandExceptionType(Component.literal(
            "An item cannot have both its buy and sell price disabled."
        ).withStyle(ChatFormatting.RED));
}
