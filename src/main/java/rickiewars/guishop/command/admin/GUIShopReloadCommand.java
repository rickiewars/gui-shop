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

public class GUIShopReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
        dispatcher.register(Commands.literal("guishop")
            .then(Commands.literal("reload")
                .requires(GuiShopPermission.RELOAD.require())
                .executes(GUIShopReloadCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> context){
        try {
            ConfigManager.loadConfig();
        } catch (IOException e) {
            context.getSource().sendFailure(Component.literal("Error accrued while reloading config!").withStyle(ChatFormatting.RED));
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
