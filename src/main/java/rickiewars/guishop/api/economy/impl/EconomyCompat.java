package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;

import java.math.BigInteger;

// BigInteger-based facade over EconomyAccount/EconomyCurrency/EconomyTransaction, whose own
// methods are BigInteger-typed from 26.1 onward but long-typed before that.
public interface EconomyCompat {

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

    static BigInteger balance(EconomyAccount account) {
        return toBig(account.balance());
    }

    static void setBalance(EconomyAccount account, BigInteger value) {
        account.setBalance(toLibrary(value));
    }

    static EconomyTransaction canIncreaseBalance(EconomyAccount account, BigInteger value) {
        return account.canIncreaseBalance(toLibrary(value));
    }

    static EconomyTransaction canDecreaseBalance(EconomyAccount account, BigInteger value) {
        return account.canDecreaseBalance(toLibrary(value));
    }

    static EconomyTransaction increaseBalance(EconomyAccount account, BigInteger value) {
        return account.increaseBalance(toLibrary(value));
    }

    static EconomyTransaction decreaseBalance(EconomyAccount account, BigInteger value) {
        return account.decreaseBalance(toLibrary(value));
    }

    static String formatValue(EconomyCurrency currency, BigInteger value, boolean precise) {
        return currency.formatValue(toLibrary(value), precise);
    }

    static BigInteger parseValue(EconomyCurrency currency, String value) {
        return toBig(currency.parseValue(value));
    }

    static BigInteger previousBalance(EconomyTransaction transaction) {
        return toBig(transaction.previousBalance());
    }

    static BigInteger finalBalance(EconomyTransaction transaction) {
        return toBig(transaction.finalBalance());
    }

    static BigInteger transactionAmount(EconomyTransaction transaction) {
        return toBig(transaction.transactionAmount());
    }
}
