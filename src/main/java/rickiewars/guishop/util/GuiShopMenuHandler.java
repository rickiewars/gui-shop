package rickiewars.guishop.util;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuConfig;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.gui.ShopMenu;
import rickiewars.guishop.shop.gui.ShopMenuContext;

public class GuiShopMenuHandler {
    private static final int LAST_DYNAMIC_SLOT = 5 * 9 - 1;
    public static void open(ServerPlayerEntity player, Shop shop) {
        MenuConfig config = new MenuConfig(
            6,
            9,
            LAST_DYNAMIC_SLOT + 1,
            LAST_DYNAMIC_SLOT + 4,
            LAST_DYNAMIC_SLOT + 5,
            LAST_DYNAMIC_SLOT + 6,
            LAST_DYNAMIC_SLOT + 9,
            true
        );
        Menu menu = new ShopMenu(shop, new MinecraftPlayer(player), config);
        MenuController menuController = new MinecraftMenuController(
            ScreenHandlerType.GENERIC_9X6,
            player,
            false
        );
        ShopMenuContext menuCtx = new ShopMenuContext(menuController, new MinecraftPlayer(player), menu);
        menuCtx.open();
    }
}
