package rickiewars.guishop.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Vanilla permission levels:
 * 0: Normal player
 * 1: Moderators can bypass spawn protection
 * 2: Gamemasters have access to commands like /scoreboard, /advancement, /function, etc. and can use command blocks
 * 3: Admins have access to most commands including /op and /deop. So effectively these players can do anything.
 * 4: Owner has access to all commands
 */
public enum GuiShopPermission {
    SELL("guishop.sell", 2),
    SELL_HAND("guishop.sell.hand", 2),
    OPEN("guishop.open", 0),
    OPEN_FOR_PLAYER("guishop.open.player", 2),
    HELP("guishop.help", 2),
    MAIN("guishop.main", 0),
    LIST("guishop.list", 0),
    LIST_ITEMS("guishop.list.items", 2),
    ADD_ITEM("guishop.additem", 2),
    REMOVE_ITEM("guishop.removeitem", 2),
    CREATE_SHOP("guishop.create", 2),
    DELETE_SHOP("guishop.delete", 2),
    FORCE_SAVE("guishop.forcesave", 2),
    RELOAD("guishop.reload", 2),
    BALANCE("guishop.balance", 2),
    BALANCE_ADD("guishop.balance.add", 3),
    BALANCE_REMOVE("guishop.balance.remove", 3),
    BALANCE_SEND("guishop.balance.send", 2),
    TEST("guishop.test", 3);



    private final String permission;
    private final int defaultLevel;

    GuiShopPermission(String permission, int defaultLevel) {
        this.permission = permission;
        this.defaultLevel = defaultLevel;
    }

    @NotNull
    public Predicate<CommandSourceStack> require() {
        return Permissions.require(this.permission, this.defaultLevel);
    }
}
