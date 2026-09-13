package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;

import java.math.BigInteger;

// BigInteger-based facade over EconomyAccount/EconomyCurrency/EconomyTransaction, whose own
// methods are BigInteger-typed from 26.1 onward but long-typed before that.
public final class EconomyCompat {
    private EconomyCompat() {}

    //? if >=26.1 {
    private static BigInteger toBig(BigInteger value) {
        return value;
    }

    private static BigInteger toLibrary(BigInteger value) {
        return value;
    }
    //?} else {
    /*private static BigInteger toBig(long value) {
        return BigInteger.valueOf(value);
    }

    private static long toLibrary(BigInteger value) {
        return value.longValueExact();
    }
    *///?}

    public static BigInteger balance(EconomyAccount account) {
        return toBig(account.balance());
    }

    public static void setBalance(EconomyAccount account, BigInteger value) {
        account.setBalance(toLibrary(value));
    }

    public static EconomyTransaction canIncreaseBalance(EconomyAccount account, BigInteger value) {
        return account.canIncreaseBalance(toLibrary(value));
    }

    public static EconomyTransaction canDecreaseBalance(EconomyAccount account, BigInteger value) {
        return account.canDecreaseBalance(toLibrary(value));
    }

    public static EconomyTransaction increaseBalance(EconomyAccount account, BigInteger value) {
        return account.increaseBalance(toLibrary(value));
    }

    public static EconomyTransaction decreaseBalance(EconomyAccount account, BigInteger value) {
        return account.decreaseBalance(toLibrary(value));
    }

    public static String formatValue(EconomyCurrency currency, BigInteger value, boolean precise) {
        return currency.formatValue(toLibrary(value), precise);
    }

    public static BigInteger parseValue(EconomyCurrency currency, String value) {
        return toBig(currency.parseValue(value));
    }

    public static BigInteger previousBalance(EconomyTransaction transaction) {
        return toBig(transaction.previousBalance());
    }

    public static BigInteger finalBalance(EconomyTransaction transaction) {
        return toBig(transaction.finalBalance());
    }

    public static BigInteger transactionAmount(EconomyTransaction transaction) {
        return toBig(transaction.transactionAmount());
    }
}
