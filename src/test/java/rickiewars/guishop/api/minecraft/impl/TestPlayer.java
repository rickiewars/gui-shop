package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestPlayer implements IPlayer {
    private final UUID uuid;
    private final TestInventory inv = new TestInventory();
//    private Map<Identifier, EconomyAccount> accounts = new HashMap<>();
    private final List<Text> receivedMessages = new ArrayList<>();

    public TestPlayer(UUID uuid) {
        this.uuid = uuid;
//        this.account = new GuiShopEconomyAccount(
//            GuiShopEconomyAccount.DEFAULT_ID,
//            new EconomyConfig.AccountDefinition(
//                GuiShopEconomyCurrency.DEFAULT_ID.getPath(),
//                "account",
//                new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
//            ),
//            this.uuid
//        );
    }

//    public void addCurrency(Identifier currency) {
//        this.accounts.put(account.currency().id(), account);
//    }
//
//    public void addDefaultAccount() {
//        this.addCurrency(new GuiShopEconomyAccount(
//            GuiShopEconomyAccount.DEFAULT_ID,
//            new EconomyConfig.AccountDefinition(
//                GuiShopEconomyCurrency.DEFAULT_ID.getPath(),
//                "account",
//                new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
//            ),
//            this.uuid
//        ));
//    }

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
        return null;
    }

    @Override
    public void sendMessage(Text message) {
        receivedMessages.add(message);
    }

    @Override
    public String getUuid() {
        return this.uuid.toString();
    }
}
