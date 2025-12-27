package rickiewars.guishop.util;

import net.minecraft.server.network.ServerPlayerEntity;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuContext;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;

public class GuiShopMenuHandler {
    public static void open(ServerPlayerEntity player, Shop shop) {
        Menu menu = new ShopMenu(shop, new MinecraftPlayer(player));
        MenuController controller = new MinecraftMenuController(menu, player, false);
        MenuContext menuCtx = new MenuContext(controller, new MinecraftPlayer(player), menu);
        menuCtx.open();
    }
}
