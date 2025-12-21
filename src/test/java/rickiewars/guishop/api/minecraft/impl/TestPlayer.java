package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.config.EconomyConfig;

import java.util.*;

public class TestPlayer implements IPlayer {
    private final UUID uuid;
    private final TestInventory inv = new TestInventory();
    private final Map<Identifier, EconomyAccount> accounts = new HashMap<>();
    private final List<Text> receivedMessages = new ArrayList<>();
    private ItemStack cursorStack = ItemStack.EMPTY;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private float yaw = 0;

    public TestPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public void addAccount(Identifier currencyId, EconomyAccount account) {
        this.accounts.put(currencyId, account);
    }

    public void addDefaultAccount(Identifier currencyId) {
        this.addAccount(currencyId, new GuiShopEconomyAccount(
            GuiShopEconomyAccount.DEFAULT_ID,
            new EconomyConfig.AccountDefinition(
                currencyId.getPath(),
                "account",
                new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
            ),
            this.uuid
        ));
    }

    public List<Text> getReceivedMessages() {
        return receivedMessages;
    }

    public void clearMessages() {
        receivedMessages.clear();
    }

    @Override
    public IInventory getInventory() {
        return inv;
    }

    @Override
    public EconomyAccount getAccount(Identifier currencyId) {
        return accounts.get(currencyId);
    }

    @Override
    public void sendMessage(Text message) {
        receivedMessages.add(message);
    }

    @Override
    public String getUuid() {
        return this.uuid.toString();
    }

    @Override
    public ItemStack getCursorStack() {
        return cursorStack;
    }

    @Override
    public Text name() {
        return Text.literal("TestPlayer");
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        cursorStack = stack;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public BlockPos getBlockPos() {
        return new BlockPos(0, 0, 0);
    }

    @Override
    public RegistryKey<World> getWorldId() {
        return World.OVERWORLD;
    }

    @Override
    public void giveItem(ItemStack itemStack) {
        inv.offerOrDrop(itemStack);
    }

    @Override
    public ItemStack getMainHandStack() {
        return mainHandStack;
    }

    @Override
    public void setMainHandStack(ItemStack itemStack) {
        mainHandStack = itemStack;
    }
}
