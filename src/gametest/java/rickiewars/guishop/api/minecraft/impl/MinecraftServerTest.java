package rickiewars.guishop.api.minecraft.impl;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.GuiShopGameTestBase;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.IServer;

public class MinecraftServerTest extends GuiShopGameTestBase {

    @GameTest
    public void getInstanceReturnsTheWrappedServer(GameTestHelper context) {
        net.minecraft.server.MinecraftServer server = context.getLevel().getServer();
        IServer wrapped = new MinecraftServer(server);

        assertTrue(context, wrapped.getInstance() == server, "getInstance should return the wrapped server");
        context.succeed();
    }

    @GameTest
    public void getInstanceThrowsWhenServerIsNotInitialized(GameTestHelper context) {
        IServer wrapped = new MinecraftServer(null);

        boolean threw;
        try {
            wrapped.getInstance();
            threw = false;
        } catch (RuntimeException e) {
            threw = true;
        }

        assertTrue(context, threw, "getInstance should throw when the server is not initialized");
        context.succeed();
    }

    @GameTest
    public void getPlayerByUUIDFindsConnectedPlayer(GameTestHelper context) {
        ServerPlayer player = mockPlayer(context);
        IServer wrapped = new MinecraftServer(context.getLevel().getServer());

        IPlayer found = wrapped.getPlayerByUUID(player.getUUID());

        assertValueEqual(context, found.getUuid(), player.getStringUUID(), "player found by uuid");
        context.succeed();
    }
}
