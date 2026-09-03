package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.economy.EconomyUtils;

public class MinecraftPlayer implements IPlayer {
    private final ServerPlayer player;

    public MinecraftPlayer(ServerPlayer player) {
        this.player = player;
        assert this.player != null;
    }

    @Override
    public IInventory getInventory() {
        return new MinecraftInventory(player.getInventory());
    }

    @Override
    public EconomyAccount getAccount(Identifier currencyId) {
        return EconomyUtils.getDefaultAccount(player, currencyId);
    }

    @Override
    public void sendMessage(Component message) {
        player.sendSystemMessage(message);
    }

    @Override
    public String getUuid() {
        return player.getStringUUID();
    }

    @Override
    public IItemStack getCursorStack() {
        return new MinecraftItemStack(player.containerMenu.getCarried());
    }

    @Override
    public Component name() {
        return player.getName();
    }

    @Override
    public void setCursorStack(IItemStack stack) {
        player.containerMenu.setCarried(unwrap(stack));
    }

    @Override
    public float getYaw() {
        return player.getYRot();
    }
    @Override
    public BlockPos getBlockPos() {
        return player.blockPosition();
    }

    @Override
    public ResourceKey<Level> getWorldId() {
        return player.level().dimension();
    }

    @Override
    public void giveItem(IItemStack itemStack) {
        player.getInventory().placeItemBackInInventory(unwrap(itemStack));
    }

    @Override
    public IItemStack getMainHandStack() {
        return new MinecraftItemStack(player.getMainHandItem());
    }

    @Override
    public void setMainHandStack(IItemStack itemStack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, unwrap(itemStack));
    }

    private static ItemStack unwrap(IItemStack stack) {
        return ((MinecraftItemStack) stack).stack();
    }
}
