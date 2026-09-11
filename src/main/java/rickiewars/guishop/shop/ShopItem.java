package rickiewars.guishop.shop;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.ResourceId;
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
        ResourceId explicitCurrencyId,
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

    public ResourceId itemId() {
        return stack.itemId();
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

    public List<Component> getDescriptionAsText() {
        LinkedList<Component> resultDescription = new LinkedList<>();

        for (String line : description) {
            resultDescription.addLast(Component.literal(line));
        }
        return resultDescription;
    }

    public Component getLoreBuyPrice() {
        MutableComponent priceText = Component.literal("");

        if (buyPrice >= 0) {
            priceText.append(Component.literal("Left click to buy for ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(formatCurrency(buyPrice)).withStyle(ChatFormatting.YELLOW)));
        }

        return priceText;
    }

    public Component getLoreSellPrice() {
        MutableComponent priceText = Component.literal("");

        if (sellPrice >= 0) {
            priceText.append(Component.literal("Right click to sell for ").withStyle(ChatFormatting.RED)
                .append(Component.literal(formatCurrency(sellPrice)).withStyle(ChatFormatting.YELLOW)));
        }

        return priceText;
    }

    public Component getLoreTradeStackInstruction() {
        return Component.literal("Hold shift to trade up to a stack of items").withStyle(ChatFormatting.AQUA);
    }

    public ResourceId resolvedCurrencyId() {
        if (explicitCurrencyId == null) {
            return EconomyUtils.getFirstCurrencyId();
        }
        return explicitCurrencyId;
    }

    public EconomyCurrency currency() {
        ResourceId currencyId = resolvedCurrencyId();
        Identifier mcCurrencyId = currencyId.toIdentifier();
        for (EconomyCurrency currency : CommonEconomy.getCurrencies(GUIShop.minecraftServer.getInstance())) {
            if (currency.id().equals(mcCurrencyId)) {
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
