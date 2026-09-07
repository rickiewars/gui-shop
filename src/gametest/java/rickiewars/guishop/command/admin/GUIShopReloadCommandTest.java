package rickiewars.guishop.command.admin;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;

import java.util.LinkedList;

public class GUIShopReloadCommandTest extends CommandTestBase {

    @GameTest
    public void reloadRereadsShopsFromDisk(GameTestHelper context) {
        Shop sentinel = new Shop("reload_sentinel", "Reload Sentinel");
        GUIShop.shopStore.writeShop(sentinel);

        // Simulate memory drifting away from what is actually on disk.
        GUIShop.shops = new LinkedList<>();

        dispatch(context, "guishop reload");

        boolean found = GUIShop.shops.stream().anyMatch(s -> s.getId().equals("reload_sentinel"));
        context.assertTrue(found, "reload should re-read shops from disk");
        context.succeed();
    }

    @GameTest
    public void reloadRequiresVanillaPermission(GameTestHelper context) {
        Shop sentinel = new Shop("sweep_sentinel", "Sweep Sentinel");
        GUIShop.shopStore.writeShop(sentinel);

        assertVanillaLevel(context, GuiShopPermission.RELOAD.defaultLevel(),
            source -> {
                GUIShop.shops = new LinkedList<>();
                return dispatch(context, "guishop reload", source);
            },
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void reloadRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop reload");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.RELOAD);
        context.succeed();
    }
}
