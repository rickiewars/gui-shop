package rickiewars.guishop.shop.gui.slot;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;

import java.util.ArrayList;
import java.util.List;

public class BalanceSlot implements MenuSlot {
    private final IPlayer player;
    private final Shop shop;

    public BalanceSlot(IPlayer player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    public ItemStack icon() {
        Identifier currencyId = shop.getDefaultCurrencyId();
        EconomyCurrency currency = player.getAccount(currencyId).currency();
        return currency.icon().copy();
    }

    public Text name() {
        return Text.literal("Your balance")
            .setStyle(Style.EMPTY.withItalic(true))
            .formatted(Formatting.GREEN);
    }

    public List<Text> lore() {
        var lines = new ArrayList<Text>();
        for (var currencyId : shop.getAllCurrencyIds()) {
            EconomyAccount account = player.getAccount(currencyId);
            MutableText currencyName = account.currency().name().copy();
            Text balance = Text.literal(
                account.currency().formatValue(account.balance(), true)
            ).setStyle(Style.EMPTY.withItalic(true)).formatted(Formatting.YELLOW);

            lines.add(
                currencyName.formatted(Formatting.GREEN)
                    .append(Text.literal(": ").formatted(Formatting.GREEN))
                    .append(balance)

            );
        }

        return lines;
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
