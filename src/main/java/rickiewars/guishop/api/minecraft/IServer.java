package rickiewars.guishop.api.minecraft;

import java.util.UUID;

public interface IServer {
    net.minecraft.server.MinecraftServer getInstance();
    IPlayer getPlayerByUUID(UUID uuid);

    /// Identifies a player by name or their UUID if not found.
    default String identifyPlayer(UUID uuid) {
        IPlayer player = getPlayerByUUID(uuid);
        return player != null ? player.name().toString() : uuid.toString();
    }
}
