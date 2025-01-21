package rickiewars.guishop.economy.economyProvider;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rickiewars.guishop.config.Config;

public class GuiShopEconomyCurrency implements EconomyCurrency {
    public static String DEFAULT_ID = GuiShopEconomyProvider.ID + ":credit";
    public static final Item DEFAULT_ICON = Items.DIAMOND;

    private final Identifier id;
    private final Config.CurrencyDefinition currencyDefinition;

    public GuiShopEconomyCurrency(Identifier id, Config.CurrencyDefinition currencyDefinition) {
        this.id = id;
        this.currencyDefinition = currencyDefinition;
    }

    @Override
    public Text name() {
        return Text.literal("Currency");
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    private String valueString(long value) {
        String prefix = this.currencyDefinition.prefix;
        String suffix = this.currencyDefinition.suffix;
        return prefix + value + suffix;
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

        if (value.startsWith(prefix)) {
            value = value.substring(prefix.length());
            if (value.endsWith(suffix)) {
                value = value.substring(0, value.length() - suffix.length() - 1);
            }
        }

        return Long.parseLong(value);
    }

    @Override
    public EconomyProvider provider() {
        return GuiShopEconomyProvider.INSTANCE;
    }

    @Override
    public ItemStack icon() {
        return this.currencyDefinition.icon;
    }
}
