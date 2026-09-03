package rickiewars.guishop.api.minecraft;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IPlayer {
    IInventory getInventory();

    EconomyAccount getAccount(Identifier currencyId);

    void sendMessage(Component message);

    String getUuid();

    IItemStack getCursorStack();

    Component name();
    void setCursorStack(IItemStack stack);

    float getYaw();
    BlockPos getBlockPos();
    ResourceKey<Level> getWorldId();

    void giveItem(IItemStack itemStack);

    IItemStack getMainHandStack();
    void setMainHandStack(IItemStack itemStack);
}