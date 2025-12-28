package rickiewars.guishop.errors;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandErrors {
    private CommandErrors() {}

    public static final SimpleCommandExceptionType NEED_PLAYER =
        new SimpleCommandExceptionType(Text.literal(
            "If the command is run from the console, you must specify a player!"
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType TARGET_SELF =
        new SimpleCommandExceptionType(Text.literal(
            "You cannot target yourself!"
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType SHOP_NOT_FOUND =
        new DynamicCommandExceptionType(shop -> Text.literal(
            "Shop '" + shop + "' does not exist."
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType NO_SHOPS_AVAILABLE =
        new SimpleCommandExceptionType(Text.literal(
            "There are no shops available at the moment. Please contact an administrator."
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType SHOP_NOT_AVAILABLE =
        new DynamicCommandExceptionType(shop -> Text.literal(
            "Shop '" + shop + "' is not available."
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType HAND_EMPTY =
        new SimpleCommandExceptionType(Text.literal(
            "You must be holding an item in your main hand to do that."
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType ITEM_NOT_FOUND =
        new DynamicCommandExceptionType(item -> Text.literal(
            "Item '" + item + "' was not found in this shop."
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType ITEM_NOT_SELLABLE =
        new SimpleCommandExceptionType(Text.literal(
            "The item you are holding is not sellable in this shop."
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType NO_CURRENCIES =
        new SimpleCommandExceptionType(Text.literal(
            "There are no currencies available at the moment. Please contact an administrator."
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType CURRENCY_NOT_FOUND =
        new DynamicCommandExceptionType(currency -> Text.literal(
            "Currency '" + currency + "' does not exist."
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType ACCOUNT_NOT_FOUND =
        new DynamicCommandExceptionType(player -> Text.literal(
            "Account for player '" + player + "' was not found."
        ).formatted(Formatting.RED));

    public static final SimpleCommandExceptionType AMOUNT_MUST_BE_POSITIVE =
        new SimpleCommandExceptionType(Text.literal(
            "The amount must be greater than 0."
        ).formatted(Formatting.RED));

    public static final DynamicCommandExceptionType TRANSACTION_FAILED =
        new DynamicCommandExceptionType(reason -> Text.literal(
            "Transaction failed: " + reason
        ).formatted(Formatting.RED));
}
