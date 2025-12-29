package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.economy.EconomyUtils;

public class MinecraftPlayer implements IPlayer {
    private final ServerPlayerEntity player;

    public MinecraftPlayer(ServerPlayerEntity player) {
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
    public void sendMessage(Text message) {
        player.sendMessage(message);
    }

    @Override
    public String getUuid() {
        return player.getUuidAsString();
    }

    @Override
    public ItemStack getCursorStack() {
        return player.currentScreenHandler.getCursorStack();
    }

    @Override
    public Text name() {
        return player.getName();
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        player.currentScreenHandler.setCursorStack(stack);
    }

    @Override
    public float getYaw() {
        return player.getYaw();
    }
    @Override
    public BlockPos getBlockPos() {
        return player.getBlockPos();
    }

    @Override
    public RegistryKey<World> getWorldId() {
        return player.getEntityWorld().getRegistryKey();
    }

    @Override
    public void giveItem(ItemStack itemStack) {
        player.getInventory().offerOrDrop(itemStack);
    }

    @Override
    public ItemStack getMainHandStack() {
        return player.getMainHandStack();
    }

    @Override
    public void setMainHandStack(ItemStack itemStack) {
        player.setStackInHand(Hand.MAIN_HAND, itemStack);
    }
}
