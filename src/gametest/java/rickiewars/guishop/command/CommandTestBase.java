package rickiewars.guishop.command;

import com.mojang.brigadier.ParseResults;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import rickiewars.guishop.GuiShopGameTestBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class CommandTestBase extends GuiShopGameTestBase {

    private static final Set<String> checkedPermissionNodes = new HashSet<>();
    static {
        PermissionCheckEvent.EVENT.register((source, permission) -> {
            checkedPermissionNodes.add(permission);
            return TriState.DEFAULT;
        });
    }

    /** Clears permission checks recorded by the previous test so they can't bleed into this one. */
    @Override
    protected void setUp(GameTestHelper context) {
        super.setUp(context);
        checkedPermissionNodes.clear();
    }

    /** Asserts exactly the given nodes were checked since the start of this test method. */
    protected static void assertPermissionsChecksHaveReceived(GameTestHelper context, GuiShopPermission... expected) {
        Set<String> expectedNodes = new HashSet<>();
        for (GuiShopPermission permission : expected) {
            expectedNodes.add(permission.node());
        }
        context.assertTrue(checkedPermissionNodes.equals(expectedNodes),
            "expected checked permission nodes " + expectedNodes + " but got " + checkedPermissionNodes);
    }

    /** Starting point for a mock player dispatch with permission level 4 (Operator). */
    protected static PlayerSource playerSource(ServerPlayer player) {
        return new PlayerSource(player, 4);
    }

    /** Sweeps every vanilla level, asserting the command succeeds at/above {@code requiredLevel} and is denied below it. */
    protected static void assertVanillaLevel(GameTestHelper context, int requiredLevel, Function<PlayerSource, DispatchResult> action, Predicate<DispatchResult> succeeded) {
        for (int level = 0; level <= 4; level++) {
            PlayerSource source = playerSource(mockPlayer(context)).withPermission(level);
            DispatchResult result = action.apply(source);
            boolean expectedToSucceed = level >= requiredLevel;
            boolean actuallySucceeded = succeeded.test(result);
            context.assertTrue(actuallySucceeded == expectedToSucceed,
                "level " + level + " (requires " + requiredLevel + "): expected "
                    + (expectedToSucceed ? "success" : "denial") + " but got " + (actuallySucceeded ? "success" : "denial"));
        }
    }

    /** Dispatches as if run from the server console, at full operator level. */
    protected static DispatchResult dispatch(GameTestHelper context, String command) {
        MinecraftServer server = context.getLevel().getServer();
        return dispatch(server, server.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS), command);
    }

    /** Dispatches as a mocked player. */
    protected static DispatchResult dispatch(GameTestHelper context, String command, PlayerSource source) {
        MinecraftServer server = context.getLevel().getServer();
        PermissionSet levelPermissions = LevelBasedPermissionSet.forLevel(PermissionLevel.byId(source.level));
        return dispatch(server, source.player.createCommandSourceStack().withPermission(levelPermissions), command);
    }

    private static DispatchResult dispatch(MinecraftServer server, CommandSourceStack baseSource, String command) {
        CapturingSource capture = new CapturingSource();
        DispatchResult result = new DispatchResult(capture);
        CommandSourceStack source = baseSource
            .withSource(capture)
            .withCallback((ok, r) -> {
                result.success = ok;
                result.result = r;
            });
        ParseResults<CommandSourceStack> parsed = server.getCommands().getDispatcher().parse(command, source);
        server.getCommands().performCommand(parsed, command);
        return result;
    }

    /** Mocked command source capturing messages */
    protected static class CapturingSource implements CommandSource {
        private final List<Component> messages = new ArrayList<>();

        public CapturingSource() {
        }

        @Override
        public void sendSystemMessage(Component component) {
            messages.add(component);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        public boolean anyMessageContains(String text) {
            return messages.stream().anyMatch(m -> m.getString().contains(text));
        }
    }

    /** Mocked command result which holds command results */
    protected static class DispatchResult {
        public boolean success;
        public int result;
        private final CapturingSource capture;

        public DispatchResult(CapturingSource capture) {
            this.capture = capture;
        }

        public boolean anyMessageContains(String text) {
            return capture.anyMessageContains(text);
        }
    }

    /** Builds the {@link CommandSourceStack} for a mock player dispatch at a given vanilla permission level. */
    public static class PlayerSource {
        private final ServerPlayer player;
        private int level;

        private PlayerSource(ServerPlayer player, int level) {
            this.player = player;
            this.level = level;
        }

        public ServerPlayer player() {
            return player;
        }

        public PlayerSource withPermission(int level) {
            this.level = level;
            return this;
        }
    }

}
