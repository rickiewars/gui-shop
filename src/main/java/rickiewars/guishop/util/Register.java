package rickiewars.guishop.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import rickiewars.guishop.command.admin.*;
import rickiewars.guishop.command.economy.GUIShopBalanceCommand;
import rickiewars.guishop.command.player.GUIShopListCommand;
import rickiewars.guishop.command.player.GUIShopMainCommand;
import rickiewars.guishop.command.player.GUIShopOpenCommand;
import rickiewars.guishop.command.player.SellCommand;

public class Register {
    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register(GUIShopMainCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopAddItemCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopAddHeldItemCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopDeleteCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopListCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopOpenCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopCreateCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopReloadCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopRemoveItemCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopForceSaveCommand::register);
        CommandRegistrationCallback.EVENT.register(SellCommand::register);
        CommandRegistrationCallback.EVENT.register(GUIShopBalanceCommand::register);
    }
}
