package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyCurrency;

import java.math.BigInteger;

// Bridges common-economy-api's BigInteger-based EconomyCurrency (26.1+) and long-based one (older).
public abstract class EconomyCurrencyCompat implements EconomyCurrency {

    protected abstract String guiShopFormatValue(BigInteger value, boolean precise);

    protected abstract BigInteger guiShopParseValue(String value) throws NumberFormatException;

    //? if >=26.1 {
    @Override
    public final String formatValue(BigInteger value, boolean precise) {
        return this.guiShopFormatValue(value, precise);
    }

    @Override
    public final BigInteger parseValue(String value) throws NumberFormatException {
        return this.guiShopParseValue(value);
    }
    //?} else {
    /*@Override
    public final String formatValue(long value, boolean precise) {
        return this.guiShopFormatValue(BigInteger.valueOf(value), precise);
    }

    @Override
    public final long parseValue(String value) throws NumberFormatException {
        return this.guiShopParseValue(value).longValueExact();
    }
    *///?}
}
