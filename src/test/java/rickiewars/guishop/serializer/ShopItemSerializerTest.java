package rickiewars.guishop.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.shop.ShopItem;

import static org.junit.jupiter.api.Assertions.*;

public class ShopItemSerializerTest {
    @Test
    void serializerProducesExpectedJsonShape() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME, net.minecraft.text.Text.literal("Legendary Sword"));
        ComponentChanges changes = stack.getComponentChanges();

        ShopItem item = new ShopItem(
            "Basic Item",
            "minecraft:stone",
            1,
            0,
            Identifier.of("guishop:testcurrency"),
            new String[]{"Line 1", "Line 2"},
            changes
        );

        JsonElement el = ConfigManager.GSON.toJsonTree(item);
        assertTrue(el.isJsonObject());
        JsonObject json = el.getAsJsonObject();

        assertEquals("Basic Item", json.get("name").getAsString());
        assertEquals("minecraft:stone", json.get("itemId").getAsString());
        assertEquals(1, json.get("buyPrice").getAsLong());
        assertEquals(0, json.get("sellPrice").getAsLong());
        assertEquals("guishop:testcurrency", json.get("currency").getAsString());

        // description must be an array
        assertTrue(json.get("description").isJsonArray());
        assertEquals(2, json.get("description").getAsJsonArray().size());

        // components must exist and be an object
        assertTrue(json.has("components"));
        assertTrue(json.get("components").isJsonObject());
        JsonObject jsonCompenents = json.get("components").getAsJsonObject();

        assertEquals("Legendary Sword", jsonCompenents.get("minecraft:custom_name").getAsString());
    }

    @Test
    void deserializerProducesOriginalItem() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME, net.minecraft.text.Text.literal("Legendary Sword"));
        ComponentChanges changes = stack.getComponentChanges();

        ShopItem original = new ShopItem(
            "Test Sword",
            "minecraft:diamond_sword",
            100,
            50,
            Identifier.of("guishop:testcurrency"),
            new String[]{"A fine blade"},
            changes
        );

        String json = ConfigManager.GSON.toJson(original);
        ShopItem restored = ConfigManager.GSON.fromJson(json, ShopItem.class);

        assertEquals(original.itemName(), restored.itemName());
        assertEquals(original.itemId(), restored.itemId());
        assertEquals(original.buyItemPrice(), restored.buyItemPrice());
        assertEquals(original.sellItemPrice(), restored.sellItemPrice());
        assertEquals(original.currencyId(), restored.currencyId());

        assertArrayEquals(original.description(), restored.description());
        assertTrue(restored.hasCurrency());
        assertTrue(restored.hasComponentChanges());

        assertEquals(
            original.componentChanges().get(DataComponentTypes.CUSTOM_NAME),
            restored.componentChanges().get(DataComponentTypes.CUSTOM_NAME)
        );
    }

    // TODO: Add tests for more component types
}
