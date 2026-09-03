package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.config.GuiShopConfig;
import rickiewars.guishop.util.CommonMethods;

import java.util.UUID;

public class GuiShopEconomyAccount implements EconomyAccount {
    public static Identifier DEFAULT_ID = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID,"account");
    public static final Item DEFAULT_ICON = Items.DIAMOND;
    public static final Identifier DEFAULT_ICON_ID = BuiltInRegistries.ITEM.getKey(DEFAULT_ICON);
    private final Identifier id;
    private final UUID uuid;
    private final String uuidString;
    private final GuiShopConfig.AccountDefinition accountDefinition;

    private DatabaseManager db() {
        return GUIShop.databaseManager;
    }

    public GuiShopEconomyAccount(Identifier accountId, GuiShopConfig.AccountDefinition accountDefinition, UUID uuid) {
        this.id = accountId;
        this.accountDefinition = accountDefinition;
        this.uuid = uuid;
        this.uuidString = uuid.toString();
    }
    @Override
    public Component name() {
        return Component.literal(accountDefinition.name);
    }

    @Override
    public UUID owner() {
        return uuid;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long balance() {
        return this.db().getBalance(
            this.currency().id().toString(),
            uuid.toString()
        );
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        long currentBal = this.balance();
        long newBal = currentBal+value;
        // 2 billion should be plenty for a player's balance
        if (newBal >= Integer.MAX_VALUE) {
            return new EconomyTransaction.Simple(
                false,
                Component.literal("Congratulations! You have hit the limit of " + currency().formatValue(Integer.MAX_VALUE, false) + ". Go spend some money so we can give you money again!"),
                currentBal,
                currentBal,
                0,
                this
            );
        }

        return new EconomyTransaction.Simple(
            true,
            Component.literal("Added " + currency().formatValue(value, false) + " to your account"),
            newBal,
            currentBal,
            value,
            this
        );
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        long currentBal = this.balance();
        long newBal = currentBal - value;
        if (newBal < 0) {
            return new EconomyTransaction.Simple(
                false,
                Component.literal("You don't have enough money to take " + currency().formatValue(value, false) + " from your account of " + currency().formatValue(currentBal, false)),
                currentBal,
                currentBal,
                0,
                this
            );
        }

        return new EconomyTransaction.Simple(
                true,
                Component.literal("Removed " + currency().formatValue(value, false) + " from your account"),
                newBal,
                currentBal,
                value,
                this
        );
    }

    @Override
    public void setBalance(long value) {
        this.db().setBalance(
            this.currency().id().toString(),
            uuidString,
            (int)value
        );
    }

    @Override
    public EconomyProvider provider() {
        return GuiShopEconomyProvider.INSTANCE;
    }

    @Override
    public EconomyCurrency currency() {
        return GuiShopEconomyProvider.INSTANCE.getCurrency(null, accountDefinition.currencyId.getPath());
    }

    @Override
    public ItemStack accountIcon() {
        return new ItemStack(CommonMethods.getItem(accountDefinition.icon.toString(), DEFAULT_ICON));
    }
}
