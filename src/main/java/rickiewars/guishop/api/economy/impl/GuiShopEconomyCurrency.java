package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.ItemRegistry;
import rickiewars.guishop.config.GuiShopConfig;
import rickiewars.guishop.util.StringUtils;

import java.math.BigInteger;

public class GuiShopEconomyCurrency extends EconomyCurrencyCompat {
    public static ResourceId DEFAULT_ID = ResourceId.of(GuiShopEconomyProvider.ID, "credit");
    public static final Item DEFAULT_ICON = Items.DIAMOND;
    public static final ResourceId DEFAULT_ICON_ID = ItemRegistry.idOf(DEFAULT_ICON);

    private final Identifier id;
    private final GuiShopConfig.CurrencyDefinition currencyDefinition;

    public GuiShopEconomyCurrency(ResourceId id, GuiShopConfig.CurrencyDefinition currencyDefinition) {
        this.id = id.toIdentifier();
        this.currencyDefinition = currencyDefinition;
    }

    @Override
    public Component name() {
        return Component.literal(this.currencyDefinition.name);
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    private String valueString(BigInteger value) {
        String prefix = this.currencyDefinition.prefix;
        String suffix = this.currencyDefinition.suffix;
        int decimalPlaces = this.currencyDefinition.decimalPlaces;
        if (decimalPlaces == 0) {
            return prefix + value + suffix;
        }

        String valueString = value.toString();
        if (valueString.length() <= decimalPlaces + 1) {
            valueString = StringUtils.padLeft(valueString, decimalPlaces + 1, '0');
        }
        return prefix
            + StringUtils.insert(valueString, -decimalPlaces, '.')
            + suffix;
    }

    @Override
    protected String guiShopFormatValue(BigInteger value, boolean precise) {
        if (precise) return valueString(value);
        if (value.signum() < 0) return valueString(BigInteger.ZERO);

        return valueString(value);
    }

    @Override
    protected BigInteger guiShopParseValue(String value) throws NumberFormatException {
        if (value.isEmpty()) return BigInteger.ZERO;

        String prefix = this.currencyDefinition.prefix;
        String suffix = this.currencyDefinition.suffix;
        int decimalPlaces = this.currencyDefinition.decimalPlaces;

        if (value.startsWith(prefix)) {
            value = value.substring(prefix.length());
            if (value.endsWith(suffix)) {
                value = value.substring(0, value.length() - suffix.length());
            }
        }

        if (value.isEmpty()) return BigInteger.ZERO;
        if (decimalPlaces == 0) return new BigInteger(value);

        value = value.replace(".", "");
        return new BigInteger(value);
    }

    @Override
    public EconomyProvider provider() {
        return GuiShopEconomyProvider.INSTANCE;
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ItemRegistry.getOptional(this.currencyDefinition.icon).orElse(DEFAULT_ICON));
    }
}
