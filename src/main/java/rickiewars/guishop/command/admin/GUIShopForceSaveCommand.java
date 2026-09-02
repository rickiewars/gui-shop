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

public class GUIShopForceSaveCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
            .then(CommandManager.literal("forcesave")
                .requires(GuiShopPermission.FORCE_SAVE.require())
                .executes(GUIShopForceSaveCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        try {
            ConfigManager.saveConfig();
            if (GUIShop.shopStore != null) {
                GUIShop.shops.forEach(GUIShop.shopStore::writeShop);
            }
            context.getSource().sendFeedback(()-> Text.literal("Config and shops successfully saved!").formatted(Formatting.GREEN), false);
        } catch (IOException e){
            context.getSource().sendFeedback(()-> Text.literal("Error saving config to file").formatted(Formatting.RED), false);
            GUIShop.LOGGER.error("Error saving config to file, IOException: ", e);
            throw new RuntimeException(e);
        } catch (Exception e){
            context.getSource().sendFeedback(()-> Text.literal("Error parsing data!").formatted(Formatting.RED), false);
            GUIShop.LOGGER.error("Error saving config to file, Exception: ", e);
            throw new RuntimeException(e);
        }

        return 0;
    }
}
