package rickiewars.guishop;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Suite
@SelectPackages({
    "rickiewars.guishop.api.database",
    "rickiewars.guishop.api.economy",
    "rickiewars.guishop.config",
    "rickiewars.guishop.serializer",
    "rickiewars.guishop.shop",
})
public class GUIShopTest {
    @BeforeAll
    static void setup() {
        initMinecraft();
    }

    @Test
    public void testThatTrueIsTrue() {
        assertTrue(true);
    }

    public static void initMinecraft() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }
}

// Fix for problem bootstrap not always running:

//// MinecraftBootstrapExtension.java
//package rickiewars.guishop.testutil;
//
//import net.minecraft.Bootstrap;
//import net.minecraft.SharedConstants;
//import org.junit.jupiter.api.extension.BeforeAllCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//
//public class MinecraftBootstrapExtension implements BeforeAllCallback {
//
//    private static boolean bootstrapped = false;
//
//    @Override
//    public void beforeAll(ExtensionContext context) {
//        if (bootstrapped) return;
//
//        SharedConstants.createGameVersion();
//        Bootstrap.initialize();
//
//        bootstrapped = true;
//    }
//}

// Use with:

//@ExtendWith(MinecraftBootstrapExtension.class)
//class GuiShopEconomyCurrencyTest {
//    ...
//}

// Global solution:
//// src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension
//rickiewars.guishop.testutil.MinecraftBootstrapExtension

// This way you don't longer need @ExtendWith
