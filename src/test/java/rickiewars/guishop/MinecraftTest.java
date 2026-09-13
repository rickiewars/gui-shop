package rickiewars.guishop;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import rickiewars.guishop.api.minecraft.impl.MinecraftCompat;

//? if >=26.1 {
//?}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinecraftTest {

    @BeforeAll
    final void setupMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        //? if >=26.1 {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
            .build(MinecraftCompat.vanillaRegistries())
            .forEach(DataComponentInitializers.PendingComponents::apply);
        //?}
    }
}
