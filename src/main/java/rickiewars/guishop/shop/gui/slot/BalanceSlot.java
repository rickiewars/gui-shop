package rickiewars.guishop.shop.gui.slot;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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

    public Component name() {
        return Component.literal("Your balance")
            .setStyle(Style.EMPTY.withItalic(true))
            .withStyle(ChatFormatting.GREEN);
    }

    public List<Component> lore() {
        var lines = new ArrayList<Component>();
        for (var currencyId : shop.getAllCurrencyIds()) {
            EconomyAccount account = player.getAccount(currencyId);
            MutableComponent currencyName = account.currency().name().copy();
            Component balance = Component.literal(
                account.currency().formatValue(account.balance(), true)
            ).setStyle(Style.EMPTY.withItalic(true)).withStyle(ChatFormatting.YELLOW);

            lines.add(
                currencyName.withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                    .append(balance)

            );
        }

        return lines;
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
