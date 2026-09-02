package rickiewars.guishop.shop;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.economy.EconomyUtils;

import java.util.*;

/**
 * An item that can be bought or sold in a shop
 */
public record ShopItem(
        String displayName,
        IItemStack stack,
        long buyPrice,
        long sellPrice,
        @Nullable
        Identifier explicitCurrencyId,
        List<String> description
) {
    private static final Set<IItemStack.ComponentKey> GRADED_COMPONENTS = Set.of(
            IItemStack.ComponentKey.DAMAGE,
            IItemStack.ComponentKey.REPAIR_COST
    );
    private static final Set<IItemStack.ComponentKey> FLAT_COMPONENTS = Set.of(
            IItemStack.ComponentKey.CUSTOM_NAME,
            IItemStack.ComponentKey.LORE
    );

    /// Check if the ShopItem has component changes like Enchantments, a custom name or description
    public boolean hasComponentChanges() {
        return stack.hasComponentChanges();
    }

    public Identifier itemId() {
        return Identifier.of(stack.itemId().namespace(), stack.itemId().path());
    }

    public int getMaxStackSize() {
        return stack.maxStackSize();
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
                && stack.equalsExact(other.stack);
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + Long.hashCode(buyPrice);
        result = 31 * result + Long.hashCode(sellPrice);
        result = 31 * result + Objects.hashCode(explicitCurrencyId);
        result = 31 * result + description.hashCode();
        result = 31 * result + stack.hashCode();
        return result;
    }

    /// Strict-policy match used to decide sale eligibility: every component must be identical
    /// except DAMAGE/REPAIR_COST (graded) and CUSTOM_NAME/LORE (flat), which are priced instead
    /// of being allowed to block a sale outright.
    public boolean resembles(IItemStack other) {
        return stack.equalsIgnoringComponents(other, ignoredForResemblance());
    }

    /// Strict match AND every graded/flat component identical to the listing too. Used only for
    /// bulk "search the whole inventory" selling, where a single flat price must apply to every
    /// stack matched, see Shop/Transaction for why this must never allow a discounted item through.
    public boolean matches(IItemStack other) {
        return resembles(other) && stack.equalsExact(other);
    }

    private static Set<IItemStack.ComponentKey> ignoredForResemblance() {
        Set<IItemStack.ComponentKey> ignored = EnumSet.copyOf(GRADED_COMPONENTS);
        ignored.addAll(FLAT_COMPONENTS);
        return ignored;
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
