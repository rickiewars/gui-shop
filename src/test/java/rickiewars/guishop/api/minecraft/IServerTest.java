package rickiewars.guishop.api.minecraft;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.TestServer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IServerTest extends MinecraftTest {

    @Test
    void identifyPlayerReturnsPlayerNameWhenPlayerFound() {
        UUID uuid = UUID.randomUUID();
        IServer server = new TestServer();

        String expected = server.getPlayerByUUID(uuid).name().toString();
        assertEquals(expected, server.identifyPlayer(uuid));
    }

    @Test
    void identifyPlayerReturnsUuidStringWhenPlayerNotFound() {
        UUID uuid = UUID.randomUUID();
        IServer server = new IServer() {
            @Override
            public MinecraftServer getInstance() {
                return null;
            }

            @Override
            public IPlayer getPlayerByUUID(UUID id) {
                return null;
            }
        };

        assertEquals(uuid.toString(), server.identifyPlayer(uuid));
    }
}
