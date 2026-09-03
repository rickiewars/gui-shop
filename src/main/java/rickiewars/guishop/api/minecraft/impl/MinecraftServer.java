package rickiewars.guishop.api.minecraft.impl;

import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.IServer;

import java.util.UUID;

public class MinecraftServer implements IServer {
    private final net.minecraft.server.MinecraftServer minecraftServer;

    public MinecraftServer(net.minecraft.server.MinecraftServer server) {
        this.minecraftServer = server;
    }

    public net.minecraft.server.MinecraftServer getInstance() {
        if (minecraftServer == null) {
            throw new RuntimeException("Server not initialized");
        }
        return minecraftServer;
    }

    public IPlayer getPlayerByUUID(UUID uuid) {
        return new MinecraftPlayer(getInstance().getPlayerList().getPlayer(uuid));
    }
}
