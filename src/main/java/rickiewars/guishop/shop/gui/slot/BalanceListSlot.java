package rickiewars.guishop.shop.gui.slot;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.api.economy.impl.EconomyCompat;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.shop.Shop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class BalanceListSlot implements MenuSlot {
    private static final int MAX_CURRENCIES_SHOWN = 5;

    private final IPlayer player;
    private final List<Shop> shops;

    public BalanceListSlot(IPlayer player, List<Shop> shops) {
        this.player = player;
        this.shops = shops;
    }

    public ItemStack icon() {
        return Items.DIAMOND.getDefaultInstance();
    }

    public Component name() {
        return Component.literal("Your balance")
            .setStyle(Style.EMPTY.withItalic(true))
            .withStyle(ChatFormatting.GREEN);
    }

    public List<Component> lore() {
        var currencyIds = new LinkedHashSet<ResourceId>();
        for (Shop shop : shops) {
            currencyIds.addAll(shop.getAllCurrencyIds());
        }

        var lines = new ArrayList<Component>();
        int shown = 0;
        for (var currencyId : currencyIds) {
            if (shown >= MAX_CURRENCIES_SHOWN) break;

            EconomyAccount account = player.getAccount(currencyId);
            MutableComponent currencyName = account.currency().name().copy();
            Component balance = Component.literal(
                EconomyCompat.formatValue(account.currency(), EconomyCompat.balance(account), true)
            ).setStyle(Style.EMPTY.withItalic(true)).withStyle(ChatFormatting.YELLOW);

            lines.add(
                currencyName.withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                    .append(balance)
            );
            shown++;
        }

        int remaining = currencyIds.size() - shown;
        if (remaining > 0) {
            lines.add(
                Component.literal("(+" + remaining + " more)")
                    .setStyle(Style.EMPTY.withItalic(true))
                    .withStyle(ChatFormatting.GRAY)
            );
        }

        return lines;
    }

    public void onClick(MenuContext ctx, ClickType click) {}
}
