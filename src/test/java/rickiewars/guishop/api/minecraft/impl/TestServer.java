package rickiewars.guishop.api.minecraft.impl;

import net.minecraft.server.MinecraftServer;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.IServer;

import java.util.UUID;

public class TestServer implements IServer {
    @Override
    public MinecraftServer getInstance() {
        return null;
    }

    @Override
    public IPlayer getPlayerByUUID(UUID uuid) {
        return new TestPlayer(uuid);
    }
}
