package rickiewars.guishop.api.minecraft;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface IPlayer {
    IInventory getInventory();

    EconomyAccount getAccount(Identifier currencyId);

    void sendMessage(Text message);

    String getUuid();
}