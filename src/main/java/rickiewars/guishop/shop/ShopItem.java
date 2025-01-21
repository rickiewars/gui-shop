package rickiewars.guishop.shop;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.economy.EconomyUtils;
import rickiewars.guishop.util.CommonMethods;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * An item that can be bought or sold in a shop
 */
public record ShopItem(
        String itemName,
        String itemId,
        long buyItemPrice,
        long sellItemPrice,
        @Nullable
        String currencyId,
        String[] description,
        ComponentChanges componentChanges
) {
    public boolean hasComponentChanges() {
        return !(Objects.isNull(componentChanges) || componentChanges.isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopItem shopItem = (ShopItem) o;

        if (shopItem.buyItemPrice != buyItemPrice) return false;
        if (shopItem.sellItemPrice != sellItemPrice) return false;
        if (!itemName.equals(shopItem.itemName)) return false;
        if (!itemId.equals(shopItem.itemId)) return false;

        // Compare descriptions
        if (description.length != shopItem.description.length) return false;
        for (int i = 0; i < description.length; i++) {
            if (!description[i].equals(shopItem.description[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(ItemStack other) {
        if (!matches(other.getItem())) return false;

        ComponentChanges otherComponentChanges = other.getComponentChanges();
        if (componentChanges == null && otherComponentChanges == null) return true;
        if (componentChanges == null || otherComponentChanges == null) return false;

        var damage = componentChanges.get(DataComponentTypes.DAMAGE);
        if (damage != null && damage.isPresent()) {
            if (other.getDamage() < damage.get()) return false;
        }
        var maxDamage = componentChanges.get(DataComponentTypes.DAMAGE);
        if (maxDamage != null && maxDamage.isPresent()) {
            if (other.getMaxDamage() < maxDamage.get()) return false;
        }

        // Only compare the relevant components
        ComponentType<?>[] relevantComponentTypes = new ComponentType[]{
                DataComponentTypes.CUSTOM_MODEL_DATA,
                DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                DataComponentTypes.ENCHANTMENTS,
                DataComponentTypes.STORED_ENCHANTMENTS,
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                DataComponentTypes.UNBREAKABLE,
                DataComponentTypes.RARITY,
                DataComponentTypes.FOOD,
                DataComponentTypes.FIRE_RESISTANT,
                DataComponentTypes.TOOL,
                DataComponentTypes.DYED_COLOR,
                DataComponentTypes.TRIM,
        };
        for (ComponentType<?> type : relevantComponentTypes) {
            if (!Objects.equals(componentChanges.get(type), otherComponentChanges.get(type))) {
                return false;
            }
        }

        return true;
    }

    public boolean matches(Item other) {
        return Registries.ITEM.getId(other).toString().equals(itemId);
    }


    @Override
    public int hashCode() {
        int result = itemName != null ? itemName.hashCode() : 0;
        result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
        result = 31 * result + (buyItemPrice != 0 ? Long.hashCode(buyItemPrice) : 0);
        result = 31 * result + (sellItemPrice != 0 ? Long.hashCode(sellItemPrice) : 0);
        result = 31 * result + Arrays.hashCode(description);
        result = 31 * result + (componentChanges != null ? componentChanges.hashCode() : 0);
        return result;
    }

    public List<Text> getDescriptionAsText() {
        LinkedList<Text> resultDescription = new LinkedList<>();

        for (String line : description) {
            Text insertion = Text.literal(line);
            resultDescription.addLast(insertion);
        }
        return resultDescription;
    }

    public Text getLoreBuyPrice() {
        MutableText priceText = Text.literal("");

        if (buyItemPrice > 0) {
            priceText.append(Text.literal("Left click to buy for ").formatted(Formatting.GREEN)
                    .append(Text.literal(CommonMethods.pretty(buyItemPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreSellPrice() {
        MutableText priceText = Text.literal("");

        if (sellItemPrice > 0) {
            priceText.append(Text.literal("Right click to sell for ").formatted(Formatting.RED)
                    .append(Text.literal(CommonMethods.pretty(sellItemPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreTradeStackInstruction() {
        return Text.literal("Hold shift to trade up to a stack of items").formatted(Formatting.AQUA);
    }

    @Override
    public String currencyId() {
        if (currencyId == null) {
            return EconomyUtils.getFirstCurrencyId();
        }
        return currencyId;
    }

    public boolean hasCurrency() {
        return currencyId != null;
    }
}

