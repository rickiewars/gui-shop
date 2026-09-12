package rickiewars.guishop;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.RegexFilter;
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.serializer.SnbtShopStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Stream;

public abstract class GuiShopGameTestBase implements CustomTestMethodInvoker {

    static {
        muteMockPlayerConnectionNoise();
    }

    /**
     * Every test spawns and disconnects its own mock player, which vanilla logs at INFO
     * (join, login, leave, "lost connection"). With dozens of tests this drowns out real
     * output, so those specific lines are dropped console-wide for the gametest run.
     */
    private static void muteMockPlayerConnectionNoise() {
        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            LoggerConfig rootConfig = context.getConfiguration().getRootLogger();
            rootConfig.addFilter(RegexFilter.createFilter(
                ".*(joined the game|left the game|logged in with entity id .*|lost connection: .*)",
                null, false, Filter.Result.DENY, Filter.Result.NEUTRAL));
            context.updateLoggers();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** Hook for subclasses to reset shared state before each test method runs. */
    protected void setUp(GameTestHelper context) {
        setUpCalled = true;
    }

    /** Hook for subclasses to clean up after each test method runs. */
    protected void tearDown(GameTestHelper context) {
        teardownCalled = true;
    }

    /** Spawns a fake, connected {@link ServerPlayer}. */
    @SuppressWarnings("removal")
    protected static ServerPlayer mockPlayer(GameTestHelper context) {
        return context.makeMockServerPlayerInLevel();
    }

    protected static void assertTrue(GameTestHelper context, boolean condition, String message) {
        //? if >=1.21.5 {
        context.assertTrue(condition, Component.literal(message));
        //?} else {
        /*context.assertTrue(condition, message);
        *///?}
    }

    protected static void assertFalse(GameTestHelper context, boolean condition, String message) {
        //? if >=1.21.5 {
        context.assertFalse(condition, Component.literal(message));
        //?} else {
        /*context.assertFalse(condition, message);
        *///?}
    }

    protected static void assertValueEqual(GameTestHelper context, Object actual, Object expected, String message) {
        //? if >=1.21.5 {
        context.assertValueEqual(actual, expected, Component.literal(message));
        //?} else {
        /*context.assertValueEqual(actual, expected, message);
        *///?}
    }

    @Override
    public final void invokeTestMethod(
        //? if >=1.21.11 {
        @NonNull GameTestHelper context,
        //?} else {
        /*GameTestHelper context,
        *///?}
        Method method
    ) {
        resetShops();
        Path shopStoreTempDir = installTempShopStore(context);
        setUp(context);
        assertTrue(context, setUpCalled, getClass().getSimpleName() + ".setUp() must call super.setUp()");
        try {
            try {
                method.invoke(this, context);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } finally {
            tearDown(context);
            assertTrue(context, teardownCalled, getClass().getSimpleName() + ".tearDown() must call super.tearDown()");
            cleanupShopStore(shopStoreTempDir);
        }
    }

    private boolean setUpCalled = false;
    private boolean teardownCalled = false;

    private static void resetShops() {
        GUIShop.shops = new LinkedList<>();
    }

    private static Path installTempShopStore(GameTestHelper context) {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("guishop-test-shops");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        GUIShop.shopStore = new SnbtShopStore(new MinecraftItemCodec(context.getLevel().getServer()), tempDir);
        return tempDir;
    }

    private static void cleanupShopStore(Path tempDir) {
        deleteRecursively(tempDir);
    }

    private static void deleteRecursively(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
