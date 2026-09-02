package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.config.ConfigManager;

import java.io.IOException;

public class GUIShopReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("reload")
                .requires(GuiShopPermission.RELOAD.require())
                .executes(GUIShopReloadCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context){
        try {
            ConfigManager.loadConfig();
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("Error accrued while reloading config!").formatted(Formatting.RED));
            GUIShop.LOGGER.error("Could not load config file", e);
            return -1;
        }

        if (GUIShop.shopStore != null) {
            GUIShop.shops = new java.util.LinkedList<>(GUIShop.shopStore.readAll());
            GUIShop.shops.forEach(rickiewars.guishop.shop.Shop::validate);
        }

        return 0;
    }
}
