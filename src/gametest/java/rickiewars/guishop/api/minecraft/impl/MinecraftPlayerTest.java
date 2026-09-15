package rickiewars.guishop.api.minecraft.impl;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GuiShopGameTestBase;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.ResourceId;

public class MinecraftPlayerTest extends GuiShopGameTestBase {

    @GameTest
    public void getUuidMatchesPlayerStringUuid(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        assertValueEqual(context, wrapped.getUuid(), player.getStringUUID(), "uuid");
        context.succeed();
    }

    @GameTest
    public void nameMatchesPlayerName(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        assertValueEqual(context, wrapped.name(), player.getName(), "name");
        context.succeed();
    }

    @GameTest
    public void getInventoryReflectsPlayerInventoryContents(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        assertValueEqual(context, wrapped.getInventory().count(ResourceId.ofVanilla("diamond")), 3, "inventory delegate count");
        context.succeed();
    }

    @GameTest
    public void cursorStackRoundTrips(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);
        IItemStack stack = new MinecraftItemStack(new ItemStack(Items.EMERALD, 2));

        wrapped.setCursorStack(stack);

        assertTrue(context, wrapped.getCursorStack().equalsExact(stack), "cursor stack after a set/get round trip");
        context.succeed();
    }

    @GameTest
    public void mainHandStackRoundTrips(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);
        IItemStack stack = new MinecraftItemStack(new ItemStack(Items.IRON_SWORD, 1));

        wrapped.setMainHandStack(stack);

        assertTrue(context, wrapped.getMainHandStack().equalsExact(stack), "main hand stack after a set/get round trip");
        assertValueEqual(context, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), Items.IRON_SWORD, "underlying main hand item");
        context.succeed();
    }

    @GameTest
    public void giveItemAddsToInventory(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        wrapped.giveItem(new MinecraftItemStack(new ItemStack(Items.GOLD_INGOT, 7)));

        assertValueEqual(context, wrapped.getInventory().count(ResourceId.ofVanilla("gold_ingot")), 7, "gold ingot count after giveItem");
        context.succeed();
    }

    @GameTest
    public void getYawMatchesPlayerYRot(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        assertValueEqual(context, wrapped.getYaw(), player.getYRot(), "yaw");
        context.succeed();
    }

    @GameTest
    public void getBlockPosMatchesPlayerBlockPosition(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        assertValueEqual(context, wrapped.getBlockPos(), player.blockPosition(), "block position");
        context.succeed();
    }

    @GameTest
    public void getWorldIdMatchesPlayerDimension(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IPlayer wrapped = new MinecraftPlayer(player);

        assertValueEqual(context, wrapped.getWorldId(), player.level().dimension(), "world id");
        context.succeed();
    }
}
