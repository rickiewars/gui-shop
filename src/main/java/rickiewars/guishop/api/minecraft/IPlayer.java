package rickiewars.guishop.api.minecraft;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPlayer {
    IInventory getInventory();

    EconomyAccount getAccount(Identifier currencyId);

    void sendMessage(Text message);

    String getUuid();

    IItemStack getCursorStack();

    Text name();
    void setCursorStack(IItemStack stack);

    float getYaw();
    BlockPos getBlockPos();
    RegistryKey<World> getWorldId();

    void giveItem(IItemStack itemStack);

    IItemStack getMainHandStack();
    void setMainHandStack(IItemStack itemStack);
}