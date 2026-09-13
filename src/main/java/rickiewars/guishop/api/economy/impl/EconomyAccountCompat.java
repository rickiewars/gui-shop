package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.network.chat.Component;

import java.math.BigInteger;

// Bridges common-economy-api's BigInteger-based EconomyAccount (26.1+) and long-based one (older).
public abstract class EconomyAccountCompat implements EconomyAccount {

    protected abstract BigInteger guiShopBalance();

    protected abstract void guiShopSetBalance(BigInteger value);

    protected abstract EconomyTransaction guiShopCanIncreaseBalance(BigInteger value);

    protected abstract EconomyTransaction guiShopCanDecreaseBalance(BigInteger value);

    //? if >=26.1 {
    @Override
    public final BigInteger balance() {
        return this.guiShopBalance();
    }

    @Override
    public final void setBalance(BigInteger value) {
        this.guiShopSetBalance(value);
    }

    @Override
    public final EconomyTransaction canIncreaseBalance(BigInteger value) {
        return this.guiShopCanIncreaseBalance(value);
    }

    @Override
    public final EconomyTransaction canDecreaseBalance(BigInteger value) {
        return this.guiShopCanDecreaseBalance(value);
    }

    protected final EconomyTransaction newTransaction(boolean success, Component message, BigInteger finalBalance, BigInteger previousBalance, BigInteger transactionAmount) {
        return new EconomyTransaction.Simple(success, message, finalBalance, previousBalance, transactionAmount, this);
    }
    //?} else {
    /*@Override
    public final long balance() {
        return this.guiShopBalance().longValueExact();
    }

    @Override
    public final void setBalance(long value) {
        this.guiShopSetBalance(BigInteger.valueOf(value));
    }

    @Override
    public final EconomyTransaction canIncreaseBalance(long value) {
        return this.guiShopCanIncreaseBalance(BigInteger.valueOf(value));
    }

    @Override
    public final EconomyTransaction canDecreaseBalance(long value) {
        return this.guiShopCanDecreaseBalance(BigInteger.valueOf(value));
    }

    protected final EconomyTransaction newTransaction(boolean success, Component message, BigInteger finalBalance, BigInteger previousBalance, BigInteger transactionAmount) {
        return new EconomyTransaction.Simple(success, message, finalBalance.longValueExact(), previousBalance.longValueExact(), transactionAmount.longValueExact(), this);
    }
    *///?}
}
