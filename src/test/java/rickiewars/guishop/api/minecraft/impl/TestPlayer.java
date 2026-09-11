package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyAccount;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IItemStack;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.config.GuiShopConfig;

import java.util.*;

public class TestPlayer implements IPlayer {
    private final UUID uuid;
    private final TestInventory inv = new TestInventory();
    private final Map<ResourceId, EconomyAccount> accounts = new HashMap<>();
    private final List<Component> receivedMessages = new ArrayList<>();
    private IItemStack cursorStack = new MinecraftItemStack(ItemStack.EMPTY);
    private IItemStack mainHandStack = new MinecraftItemStack(ItemStack.EMPTY);
    private float yaw = 0;

    public TestPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public void addAccount(ResourceId currencyId, EconomyAccount account) {
        this.accounts.put(currencyId, account);
    }

    public void addDefaultAccount(ResourceId currencyId) {
        this.addAccount(currencyId, new GuiShopEconomyAccount(
            GuiShopEconomyAccount.DEFAULT_ID,
            new GuiShopConfig.AccountDefinition(
                currencyId.path(),
                "account",
                GuiShopEconomyCurrency.DEFAULT_ICON_ID
            ),
            this.uuid
        ));
    }

    public List<Component> getReceivedMessages() {
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
    public EconomyAccount getAccount(ResourceId currencyId) {
        return accounts.get(currencyId);
    }

    @Override
    public void sendMessage(Component message) {
        receivedMessages.add(message);
    }

    @Override
    public String getUuid() {
        return this.uuid.toString();
    }

    @Override
    public IItemStack getCursorStack() {
        return cursorStack;
    }

    @Override
    public Component name() {
        return Component.literal("TestPlayer");
    }

    @Override
    public void setCursorStack(IItemStack stack) {
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
    public ResourceKey<Level> getWorldId() {
        return Level.OVERWORLD;
    }

    @Override
    public void giveItem(IItemStack itemStack) {
        inv.offerOrDrop(itemStack);
    }

    @Override
    public IItemStack getMainHandStack() {
        return mainHandStack;
    }

    @Override
    public void setMainHandStack(IItemStack itemStack) {
        mainHandStack = itemStack;
    }
}
