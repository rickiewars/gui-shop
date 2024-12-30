package unsafedodo.guishop.shop;

import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An item that can be bought or sold in a shop
 */
public class ShopItem {
    private String itemName;
    private String itemMaterial;
    private float buyItemPrice;
    private float sellItemPrice;
    private String[] description;
    private NbtCompound nbt;

    public ShopItem(String itemName, String itemMaterial, float buyItemPrice, float sellItemPrice, String[] description, NbtCompound nbt) {
        this.itemName = itemName;
        this.itemMaterial = itemMaterial;
        this.buyItemPrice = buyItemPrice;
        this.sellItemPrice = sellItemPrice;
        this.description = description;
        this.nbt = nbt;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemMaterial() {
        return itemMaterial;
    }

    public float getBuyItemPrice() {
        return buyItemPrice;
    }

    public float getSellItemPrice() {
        return sellItemPrice;
    }

    public String[] getDescription() {
        return description;
    }

    public NbtCompound getNbt() {
        return nbt;
    }

    public boolean hasNbt(){
        // TODO: Check if nbt.isEmpty() is equivalent to this.
        return (nbt != null) && !(nbt.toString().equals("{}"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopItem shopItem = (ShopItem) o;

        if (Float.compare(shopItem.buyItemPrice, buyItemPrice) != 0) return false;
        if (Float.compare(shopItem.sellItemPrice, sellItemPrice) != 0) return false;
        if (!itemName.equals(shopItem.itemName)) return false;
        if (!itemMaterial.equals(shopItem.itemMaterial)) return false;

        // Compare descriptions
        if (description.length != shopItem.description.length) return false;
        for (int i = 0; i < description.length; i++) {
            if (!description[i].equals(shopItem.description[i])) {
                return false;
            }
        }

        return nbt.toString().equals(shopItem.nbt.toString());
    }

    @Override
    public int hashCode() {
        int result = itemName != null ? itemName.hashCode() : 0;
        result = 31 * result + (itemMaterial != null ? itemMaterial.hashCode() : 0);
        result = 31 * result + (buyItemPrice != 0.0f ? Float.floatToIntBits(buyItemPrice) : 0);
        result = 31 * result + (sellItemPrice != 0.0f ? Float.floatToIntBits(sellItemPrice) : 0);
        result = 31 * result + Arrays.hashCode(description);
        result = 31 * result + nbt.hashCode();
        return result;
    }

    public List<Text> getDescriptionAsText(){
        LinkedList<Text> resultDescription = new LinkedList<>();

        for(String line: description){
            Text insertion = TextParserUtils.formatText(line);
            resultDescription.addLast(insertion);
        }
        return resultDescription;
    }

    public Text getLoreBuyPrice(){
        MutableText priceText = Text.literal("");

        if(buyItemPrice > 0){
            priceText.append(Text.literal("Left click to buy for ").formatted(Formatting.GREEN)
                    .append(Text.literal(String.format("%.2f $", buyItemPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreSellPrice(){
        MutableText priceText = Text.literal("");

        if(sellItemPrice > 0){
            priceText.append(Text.literal("Right click to sell for ").formatted(Formatting.RED)
                    .append(Text.literal(String.format("%.2f $", sellItemPrice)).formatted(Formatting.YELLOW)));
        }

        return priceText;
    }

    public Text getLoreTradeStackInstruction(){
        return Text.literal("Hold shift to trade up to a stack of items").formatted(Formatting.AQUA);
    }

}

