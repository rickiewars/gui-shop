package rickiewars.guishop.shop.gui.slot;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;

import java.util.List;

public class BalanceSlot implements MenuSlot {
    private final IPlayer player;
    private final Identifier currencyId;

    public BalanceSlot(IPlayer player, Identifier currencyId) {
        this.player = player;
        this.currencyId = currencyId;
    }

    public ItemStack icon() {
        if (currencyId == null) {
            return Items.DIAMOND.getDefaultStack();
        }
        EconomyCurrency currency = player.getAccount(currencyId).currency();
        return currency.icon().copy();
    }

    public Text name() {
        EconomyAccount account = player.getAccount(currencyId);

        Text balance = Text.literal(
            account.currency().formatValue(account.balance(), true)
        ).setStyle(Style.EMPTY.withItalic(true)).formatted(Formatting.YELLOW);

        return Text.literal("Your balance: ")
            .setStyle(Style.EMPTY.withItalic(true))
            .formatted(Formatting.GREEN)
            .append(balance);
    }

    public List<Text> lore() {
        return List.of();
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
