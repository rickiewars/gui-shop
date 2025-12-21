package rickiewars.guishop;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinecraftTest {

    @BeforeAll
    final void setupMinecraft() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }
}
