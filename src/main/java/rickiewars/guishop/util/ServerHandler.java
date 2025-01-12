package rickiewars.guishop.util;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ServerHandler {
    private static MinecraftServer minecraftServer;

    private static MinecraftServer server() {
        if (minecraftServer == null) {
            throw new RuntimeException("Server not initialized");
        }
        return minecraftServer;
    }

    public static void init(MinecraftServer server) {
        ServerHandler.minecraftServer = server;
    }

    public static ServerPlayerEntity getPlayerByUUID(UUID uuid) {
        return server().getPlayerManager().getPlayer(uuid);
    }

    public static DynamicRegistryManager getRegistryManager() {
        return server().getRegistryManager();
    }
}
