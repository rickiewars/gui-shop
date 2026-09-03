package rickiewars.guishop;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinecraftTest {

    @BeforeAll
    final void setupMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
