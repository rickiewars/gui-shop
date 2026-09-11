package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.ItemRegistry;
import rickiewars.guishop.config.GuiShopConfig;
import rickiewars.guishop.util.CommonMethods;

public class GuiShopEconomyCurrency implements EconomyCurrency {
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

    private String valueString(long value) {
        String prefix = this.currencyDefinition.prefix;
        String suffix = this.currencyDefinition.suffix;
        int decimalPlaces = this.currencyDefinition.decimalPlaces;
        if (decimalPlaces == 0) {
            return prefix + value + suffix;
        }

        String valueString = String.valueOf(value);
        if (valueString.length() <= decimalPlaces + 1) {
            valueString = CommonMethods.padLeft(valueString, decimalPlaces + 1, '0');
        }
        return prefix
            + CommonMethods.insert(valueString, -decimalPlaces, '.')
            + suffix;
    }

    @Override
    public String formatValue(long value, boolean precise) {
        if (precise) return valueString(value);
        if (value < 0) return valueString(0);

        return valueString(value);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        if (value.isEmpty()) return 0;

        String prefix = this.currencyDefinition.prefix;
        String suffix = this.currencyDefinition.suffix;
        int decimalPlaces = this.currencyDefinition.decimalPlaces;

        if (value.startsWith(prefix)) {
            value = value.substring(prefix.length());
            if (value.endsWith(suffix)) {
                value = value.substring(0, value.length() - suffix.length());
            }
        }

        if (value.isEmpty()) return 0;
        if (decimalPlaces == 0) return Long.parseLong(value);

        value = value.replace(".", "");
        return Long.parseLong(value);
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
