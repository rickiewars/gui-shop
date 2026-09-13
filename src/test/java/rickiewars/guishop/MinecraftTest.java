package rickiewars.guishop;

import net.minecraft.SharedConstants;
//? if >=26.1 {
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
//?}
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinecraftTest {

    @BeforeAll
    final void setupMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        //? if >=26.1 {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
            .build(VanillaRegistries.createLookup())
            .forEach(DataComponentInitializers.PendingComponents::apply);
        //?}
    }
}
