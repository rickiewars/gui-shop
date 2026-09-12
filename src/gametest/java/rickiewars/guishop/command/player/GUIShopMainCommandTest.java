package rickiewars.guishop.command.player;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.command.CommandTestBase;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.List;

public class GUIShopMainCommandTest extends CommandTestBase {

    @GameTest
    public void mainOpensSelectShopMenuForPlayerWhenStockedShopsExist(GameTestHelper context) {
        Shop shop = new Shop("stocked_shop", "Stocked Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop", playerSource(player));

        assertTrue(context, result.success, "expected the command to report success");
        assertValueEqual(context, result.result, 0, "command result code");
        context.succeed();
    }

    @GameTest
    public void mainFallsBackToHelpWhenNoPlayerSource(GameTestHelper context) {
        var result = dispatch(context, "guishop");

        assertTrue(context, result.anyMessageContains("GUIShop by Rickiewars is running!"), "expected the help text as a console fallback");
        context.succeed();
    }

    @GameTest
    public void mainReportsErrorWhenNoStockedShopsExist(GameTestHelper context) {
        GUIShop.shops.add(new Shop("empty_shop", "Empty Shop"));
        ServerPlayer player = mockPlayer(context);

        var result = dispatch(context, "guishop", playerSource(player));

        assertTrue(context, result.anyMessageContains("no shops available"), "expected a no-shops-available message");
        context.succeed();
    }

    @GameTest
    public void mainHelpSubcommandPrintsUsage(GameTestHelper context) {
        var result = dispatch(context, "guishop help");

        assertTrue(context, result.anyMessageContains("GUIShop by Rickiewars is running!"), "expected the intro text");
        assertTrue(context, result.anyMessageContains("/shop additem"), "expected the usage text");
        context.succeed();
    }

    @GameTest
    public void mainRegistersAliasesForShopBuyAndTrade(GameTestHelper context) {
        var result = dispatch(context, "buy help");

        assertTrue(context, result.anyMessageContains("GUIShop by Rickiewars is running!"), "the 'buy' alias should route to the same command tree");
        context.succeed();
    }

    @GameTest
    public void mainRequiresVanillaPermission(GameTestHelper context) {
        Shop shop = new Shop("sweep_shop", "Sweep Shop");
        shop.getItems().add(new ShopItem("Sword", new MinecraftItemStack(new ItemStack(Items.DIAMOND_SWORD)), 20, 10, null, List.of()));
        GUIShop.shops.add(shop);

        assertVanillaLevel(context, GuiShopPermission.MAIN.defaultLevel(),
            source -> dispatch(context, "guishop", source),
            result -> result.success);
        context.succeed();
    }

    @GameTest
    public void helpRequiresVanillaPermission(GameTestHelper context) {
        assertVanillaLevel(context, GuiShopPermission.HELP.defaultLevel(),
            source -> dispatch(context, "guishop help", source),
            result -> result.anyMessageContains("GUIShop by Rickiewars is running!"));
        context.succeed();
    }

    @GameTest
    public void mainRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN);
        context.succeed();
    }

    @GameTest
    public void helpRequiresFabricPermission(GameTestHelper context) {
        dispatch(context, "guishop help");
        assertPermissionsChecksHaveReceived(context, GuiShopPermission.MAIN, GuiShopPermission.HELP);
        context.succeed();
    }
}
