package rickiewars.guishop.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.util.ShopFileHandler;

import java.io.IOException;

public class GUIShopForceSaveCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        dispatcher.register(CommandManager.literal("guishop")
                .then(CommandManager.literal("forcesave")
                        .requires(Permissions.require("guishop.forcesave", 2))
                        .executes(GUIShopForceSaveCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        ShopFileHandler fileHandler = new ShopFileHandler();
        try{
            fileHandler.saveToFile();
            context.getSource().sendFeedback(()-> Text.literal("Shops successfully saved to config file!").formatted(Formatting.GREEN), false);
        } catch (IOException e){
            context.getSource().sendFeedback(()-> Text.literal("Error saving shops to file").formatted(Formatting.RED), false);
            GUIShop.LOGGER.error("Error saving shops to file, IOException: ", e);
            throw new RuntimeException(e);
        } catch (Exception e){
            context.getSource().sendFeedback(()-> Text.literal("Error parsing data!").formatted(Formatting.RED), false);
            GUIShop.LOGGER.error("Error saving shops to file, Exception: ", e);
            throw new RuntimeException(e);
        }

        return 0;
    }
}
