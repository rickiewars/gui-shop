package rickiewars.guishop.shop;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.*;

/**
 * An item that can be bought or sold in a shop
 */
public record ShopItem(
        String displayName,
        ItemStack stack,
        long buyPrice,
        long sellPrice,
        @Nullable
        Identifier explicitCurrencyId,
        List<String> description
) {
    private static final Set<ComponentType<?>> GRADED_COMPONENTS = Set.of(
            DataComponentTypes.DAMAGE,
            DataComponentTypes.REPAIR_COST
    );
    private static final Set<ComponentType<?>> FLAT_COMPONENTS = Set.of(
            DataComponentTypes.CUSTOM_NAME,
            DataComponentTypes.LORE
    );

    /// Check if the ShopItem has component changes like Enchantments, a custom name or description
    public boolean hasComponentChanges() {
        return !stack.getComponentChanges().isEmpty();
    }

    public Identifier itemId() {
        return Registries.ITEM.getId(stack.getItem());
    }

    public int getMaxStackSize() {
        return stack.getMaxCount();
    }

    /// A listing that can neither be bought nor sold should not appear in the shop GUI at all.
    public boolean isListable() {
        return buyPrice != -1 || sellPrice != -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopItem other)) return false;

        return buyPrice == other.buyPrice
                && sellPrice == other.sellPrice
                && displayName.equals(other.displayName)
                && Objects.equals(explicitCurrencyId, other.explicitCurrencyId)
                && description.equals(other.description)
                && ItemStack.areItemsAndComponentsEqual(stack, other.stack);
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + Long.hashCode(buyPrice);
        result = 31 * result + Long.hashCode(sellPrice);
        result = 31 * result + Objects.hashCode(explicitCurrencyId);
        result = 31 * result + description.hashCode();
        result = 31 * result + ItemStack.hashCode(stack);
        return result;
    }

    /// Strict-policy match used to decide sale eligibility: every component must be identical
    /// except DAMAGE/REPAIR_COST (graded) and CUSTOM_NAME/LORE (flat), which are priced instead
    /// of being allowed to block a sale outright.
    public boolean resembles(ItemStack other) {
        if (!stack.getItem().equals(other.getItem())) return false;

        ItemStack a = stack.copy();
        ItemStack b = other.copy();
        for (ComponentType<?> type : GRADED_COMPONENTS) {
            a.remove(type);
            b.remove(type);
        }
        for (ComponentType<?> type : FLAT_COMPONENTS) {
            a.remove(type);
            b.remove(type);
        }
        return ItemStack.areItemsAndComponentsEqual(a, b);
    }

    /// Strict match AND every graded/flat component identical to the listing too. Used only for
    /// bulk "search the whole inventory" selling, where a single flat price must apply to every
    /// stack matched, see Shop/Transaction for why this must never allow a discounted item through.
    public boolean matches(ItemStack other) {
        return resembles(other) && ItemStack.areItemsAndComponentsEqual(stack, other);
    }

    public List<Text> getDescriptionAsText() {
        LinkedList<Text> resultDescription = new LinkedList<>();

        for (String line : description) {
            resultDescription.addLast(Text.literal(line));
        }
        return resultDescription;
    }

    public Text getLoreBuyPrice() {
        MutableText priceText = Text.literal("");

        if (buyPrice >= 0) {
            priceText.append(Text.literal("Left click to buy for ").formatted(Formatting.GREEN)
                .append(Text.literal(formatCurrency(buyPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreSellPrice() {
        MutableText priceText = Text.literal("");

        if (sellPrice >= 0) {
            priceText.append(Text.literal("Right click to sell for ").formatted(Formatting.RED)
                .append(Text.literal(formatCurrency(sellPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreTradeStackInstruction() {
        return Text.literal("Hold shift to trade up to a stack of items").formatted(Formatting.AQUA);
    }

    public Identifier resolvedCurrencyId() {
        if (explicitCurrencyId == null) {
            return EconomyUtils.getFirstCurrencyId();
        }
        return explicitCurrencyId;
    }

    public EconomyCurrency currency() {
        Identifier currencyId = resolvedCurrencyId();
        for (EconomyCurrency currency : CommonEconomy.getCurrencies(GUIShop.minecraftServer.getInstance())) {
            if (currency.id().equals(currencyId)) {
                return currency;
            }
        }

        throw new NoSuchElementException("Could not find currency with id " + currencyId);
    }

    public String formatCurrency(long value) {
        return currency().formatValue(value, false);
    }

    public boolean hasCurrency() {
        return explicitCurrencyId != null;
    }
}
