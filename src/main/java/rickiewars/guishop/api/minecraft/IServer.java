package rickiewars.guishop.api.minecraft;

import java.util.UUID;

public interface IServer {
    net.minecraft.server.MinecraftServer getInstance();
    IPlayer getPlayerByUUID(UUID uuid);
}
