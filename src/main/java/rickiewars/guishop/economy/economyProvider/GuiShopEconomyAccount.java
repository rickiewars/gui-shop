package rickiewars.guishop.economy.economyProvider;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.Config;

import java.util.UUID;

public class GuiShopEconomyAccount implements EconomyAccount {
    public static String DEFAULT_ID = GuiShopEconomyProvider.ID + ":account";
    public static final Item DEFAULT_ICON = Items.DIAMOND;
    private final Identifier id;
    private final UUID uuid;
    private final String uuidString;
    private final Config.AccountDefinition accountDefinition;

    public GuiShopEconomyAccount(Identifier accountId, Config.AccountDefinition accountDefinition, UUID uuid) {
        this.id = accountId;
        this.accountDefinition = accountDefinition;
        this.uuid = uuid;
        this.uuidString = uuid.toString();
    }
    @Override
    public Text name() {
        return Text.literal(accountDefinition.name);
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
        // TODO: Implement this method
        return 0;
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        // TODO: Implement this method
        int currentBal = 1000;
        long newBal = (long)currentBal+value;
        // TODO: I think we can use Long.MAX_VALUE? SQLite's INTEGER can store up to 8 bytes of data, which is equivalent to that of a long
        //       To do overflow check, just check if the new balance is smaller than 0, which occurs when the value has overflown
        if (newBal >= Integer.MAX_VALUE) {
            return new EconomyTransaction.Simple(
                    false,
                    Text.literal("Congratulations! You have hit the limit of " + currency().formatValue(Integer.MAX_VALUE, false) + ". Go spend some money so we can give you money again!"),
                    currentBal,
                    currentBal,
                    0,
                    this
            );
        }

        return new EconomyTransaction.Simple(
                true,
                Text.literal("Added " + currency().formatValue(value, false) + " to the account"),
                newBal,
                currentBal,
                value,
                this
        );
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        // TODO: Implement this method
        int currentBal = 1000;
        long newBal = (long)currentBal-value;
        if (newBal < 0) {
            return new EconomyTransaction.Simple(
                    false,
                    Text.literal("You don't have enough money to take " + currency().formatValue(value, false) + " from your account of " + currency().formatValue(currentBal, false)),
                    currentBal,
                    currentBal,
                    0,
                    this
            );
        }

        return new EconomyTransaction.Simple(
                true,
                Text.literal("Removed " + currency().formatValue(value, false) + " from your account"),
                newBal,
                currentBal,
                value,
                this
        );
    }

    @Override
    public void setBalance(long value) {
        GUIShop.LOGGER.info("Setting balance of " + uuidString + " to " + value);
        // TODO: Implement this method
    }

    @Override
    public EconomyProvider provider() {
        return GuiShopEconomyProvider.INSTANCE;
    }

    @Override
    public EconomyCurrency currency() {
        return GuiShopEconomyProvider.INSTANCE.getCurrency(null, accountDefinition.currencyId);
    }

    @Override
    public ItemStack accountIcon() {
        return accountDefinition.icon;
    }
}
