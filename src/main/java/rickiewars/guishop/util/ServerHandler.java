package rickiewars.guishop.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ServerHandler {
    private static MinecraftServer minecraftServer;

    public static MinecraftServer getServer() {
        if (minecraftServer == null) {
            throw new RuntimeException("Server not initialized");
        }
        return minecraftServer;
    }

    public static void init(MinecraftServer server) {
        ServerHandler.minecraftServer = server;
    }

    public static ServerPlayerEntity getPlayerByUUID(UUID uuid) {
        return getServer().getPlayerManager().getPlayer(uuid);
    }
}
