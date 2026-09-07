package rickiewars.guishop.command.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import rickiewars.guishop.api.gui.Menu;
import rickiewars.guishop.api.gui.MenuController;
import rickiewars.guishop.api.gui.impl.MinecraftMenuController;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.devtest.gui.DevTestMenu;
import rickiewars.guishop.errors.CommandErrors;

/**
 * Opens the in-game sgui manual test menu. Only for development purposes (testing the SGUI itegration).
 */
public class GUIShopDevTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("devtest")
                .requires(GuiShopPermission.TEST.require())
                .executes(GUIShopDevTestCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) throw CommandErrors.NEED_PLAYER.create();

        Menu menu = new DevTestMenu(new MinecraftPlayer(player));
        MenuController controller = new MinecraftMenuController(player);
        return controller.open(menu) ? 0 : -1;
    }
}
