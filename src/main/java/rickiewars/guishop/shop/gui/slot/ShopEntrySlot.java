package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuSlot;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;

import java.util.List;

public class ShopEntrySlot implements MenuSlot {
    private final Shop shop;
    private final IPlayer player;

    public ShopEntrySlot(Shop shop, IPlayer player) {
        this.shop = shop;
        this.player = player;
    }

    public ItemStack icon() {
        return shop.getIcon();
    }

    public Component name() {
        return Component.literal(shop.getDisplayName());
    }

    public List<Component> lore() {
        var currencyId = shop.getDefaultCurrencyId();
        var account = player.getAccount(currencyId);
        return List.of(
            Component.literal("Main currency: ").append(account.currency().name()),
            Component.literal("Your Balance: ").append(account.formattedBalance())
        );
    }

    public void onClick(MenuContext ctx, ClickType click) {
        var menu = new ShopMenu(shop, player);
        ctx.open(menu);
    }
}