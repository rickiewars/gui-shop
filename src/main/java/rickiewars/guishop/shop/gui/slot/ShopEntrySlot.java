package rickiewars.guishop.shop.gui.slot;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

    public Text name() {
        return Text.literal(shop.getName());
    }

    public List<Text> lore() {
        var currencyId = shop.getDefaultCurrencyId();
        var account = player.getAccount(currencyId);
        return List.of(
            Text.literal("Main currency: ").append(account.currency().name()),
            Text.literal("Your Balance: ").append(account.formattedBalance())
        );
    }

    public void onClick(MenuContext ctx, ClickType click) {
        var menu = new ShopMenu(shop, player);
        ctx.open(menu);
    }
}