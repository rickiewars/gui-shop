package rickiewars.guishop.api.economy.impl;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.ItemRegistry;
import rickiewars.guishop.config.GuiShopConfig;

import java.math.BigInteger;
import java.util.UUID;

public class GuiShopEconomyAccount extends EconomyAccountCompat {
    public static ResourceId DEFAULT_ID = ResourceId.of(GuiShopEconomyProvider.ID, "account");
    public static final Item DEFAULT_ICON = Items.DIAMOND;
    public static final ResourceId DEFAULT_ICON_ID = ItemRegistry.idOf(DEFAULT_ICON);
    private final Identifier id;
    private final UUID uuid;
    private final String uuidString;
    private final GuiShopConfig.AccountDefinition accountDefinition;

    private DatabaseManager db() {
        return GUIShop.databaseManager;
    }

    public GuiShopEconomyAccount(ResourceId accountId, GuiShopConfig.AccountDefinition accountDefinition, UUID uuid) {
        this.id = accountId.toIdentifier();
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
    protected BigInteger guiShopBalance() {
        return BigInteger.valueOf(this.db().getBalance(
            this.currency().id().toString(),
            uuid.toString()
        ));
    }

    @Override
    protected EconomyTransaction guiShopCanIncreaseBalance(BigInteger value) {
        BigInteger currentBal = this.guiShopBalance();
        BigInteger newBal = currentBal.add(value);
        // Capped at Long.MAX_VALUE - 1, since the DB column is a long and rejects the sentinel Long.MAX_VALUE.
        if (newBal.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            return newTransaction(
                false,
                Component.literal(
                    "Congratulations! You have hit the limit of " +
                        EconomyCompat.formatValue(currency(), BigInteger.valueOf(Long.MAX_VALUE), false) +
                        ". Go spend some money so we can give you money again!"
                ),
                currentBal,
                currentBal,
                BigInteger.ZERO
            );
        }

        return newTransaction(
            true,
            Component.literal(
                "Added " +
                    EconomyCompat.formatValue(currency(), value, false) +
                    " to your account"
            ),
            newBal,
            currentBal,
            value
        );
    }

    @Override
    protected EconomyTransaction guiShopCanDecreaseBalance(BigInteger value) {
        BigInteger currentBal = this.guiShopBalance();
        BigInteger newBal = currentBal.subtract(value);
        if (newBal.signum() < 0) {
            return newTransaction(
                false,
                Component.literal("You don't have enough money to take " + EconomyCompat.formatValue(currency(), value, false) + " from your account of " + EconomyCompat.formatValue(currency(), currentBal, false)),
                currentBal,
                currentBal,
                BigInteger.ZERO
            );
        }

        return newTransaction(
            true,
            Component.literal("Removed " + EconomyCompat.formatValue(currency(), value, false) + " from your account"),
            newBal,
            currentBal,
            value
        );
    }

    @Override
    protected void guiShopSetBalance(BigInteger value) {
        this.db().setBalance(
            this.currency().id().toString(),
            uuidString,
            value.longValueExact()
        );
    }

    @Override
    public EconomyProvider provider() {
        return GuiShopEconomyProvider.INSTANCE;
    }

    @Override
    public EconomyCurrency currency() {
        return GuiShopEconomyProvider.INSTANCE.getCurrency(null, accountDefinition.currencyId.path());
    }

    @Override
    public ItemStack accountIcon() {
        return new ItemStack(ItemRegistry.getOptional(accountDefinition.icon).orElse(DEFAULT_ICON));
    }
}
