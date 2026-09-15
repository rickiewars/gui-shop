package rickiewars.guishop.api.minecraft.impl;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GuiShopGameTestBase;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.ResourceId;

public class MinecraftInventoryTest extends GuiShopGameTestBase {

    @GameTest
    public void sizeMatchesUnderlyingInventoryContainerSize(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());

        assertValueEqual(context, inventory.size(), player.getInventory().getContainerSize(), "inventory size");
        context.succeed();
    }

    @GameTest
    public void countReturnsZeroForItemNotPresent(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());

        assertValueEqual(context, inventory.count(ResourceId.ofVanilla("diamond")), 0, "count of absent item");
        context.succeed();
    }

    @GameTest
    public void offerOrDropAddsItemToInventory(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());
        ResourceId diamond = ResourceId.ofVanilla("diamond");

        inventory.offerOrDrop(new MinecraftItemStack(new ItemStack(Items.DIAMOND, 5)));

        assertValueEqual(context, inventory.count(diamond), 5, "diamond count after offerOrDrop");
        context.succeed();
    }

    @GameTest
    public void removeDecrementsMatchingStackUpToAmount(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());
        ResourceId diamond = ResourceId.ofVanilla("diamond");
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 10));

        int removed = inventory.remove(diamond, 4, stack -> true);

        assertValueEqual(context, removed, 4, "amount removed");
        assertValueEqual(context, inventory.count(diamond), 6, "remaining count after removal");
        context.succeed();
    }

    @GameTest
    public void removeSkipsSlotsRejectedByFilter(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());
        ResourceId diamond = ResourceId.ofVanilla("diamond");
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 10));

        int removed = inventory.remove(diamond, 4, stack -> false);

        assertValueEqual(context, removed, 0, "amount removed when the filter rejects everything");
        assertValueEqual(context, inventory.count(diamond), 10, "count unchanged when the filter rejects everything");
        context.succeed();
    }

    @GameTest
    public void removeIgnoresNonMatchingItems(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IInventory inventory = new MinecraftInventory(player.getInventory());
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 10));

        int removed = inventory.remove(ResourceId.ofVanilla("emerald"), 4, stack -> true);

        assertValueEqual(context, removed, 0, "amount removed for a non-matching item id");
        assertValueEqual(context, inventory.count(ResourceId.ofVanilla("diamond")), 10, "matching item count unaffected");
        context.succeed();
    }
}
