package rickiewars.guishop.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.config.ConfigManager;

import java.io.IOException;

public class GUIShopForceSaveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("forcesave")
                .requires(GuiShopPermission.FORCE_SAVE.require())
                .executes(GUIShopForceSaveCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> context) {
        try {
            ConfigManager.saveConfig();
            if (GUIShop.shopStore != null) {
                GUIShop.shops.forEach(GUIShop.shopStore::writeShop);
            }
            context.getSource().sendSuccess(()-> Component.literal("Config and shops successfully saved!").withStyle(ChatFormatting.GREEN), false);
        } catch (IOException e){
            context.getSource().sendSuccess(()-> Component.literal("Error saving config to file").withStyle(ChatFormatting.RED), false);
            GUIShop.LOGGER.error("Error saving config to file, IOException: ", e);
            throw new RuntimeException(e);
        } catch (Exception e){
            context.getSource().sendSuccess(()-> Component.literal("Error parsing data!").withStyle(ChatFormatting.RED), false);
            GUIShop.LOGGER.error("Error saving config to file, Exception: ", e);
            throw new RuntimeException(e);
        }

        return 0;
    }
}
