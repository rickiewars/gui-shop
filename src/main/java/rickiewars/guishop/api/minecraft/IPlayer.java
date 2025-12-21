package rickiewars.guishop.api.minecraft;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.item.ItemStack;
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

    ItemStack getCursorStack();

    Text name();
    void setCursorStack(ItemStack stack);

    float getYaw();
    BlockPos getBlockPos();
    RegistryKey<World> getWorldId();

    void giveItem(ItemStack itemStack);

    ItemStack getMainHandStack();
    void setMainHandStack(ItemStack itemStack);
}