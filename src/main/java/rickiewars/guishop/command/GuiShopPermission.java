package rickiewars.guishop.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

// TODO: Recheck the vanilla permission levels
public enum GuiShopPermission {
    SELL_HAND("guishop.sell.hand", 2),
    // SELL_ALL("guishop.sell.all", 2),
    OPEN("guishop.open", 2),
    HELP("guishop.help", 2),
    MAIN("guishop.main", 2),
    LIST("guishop.list", 3),
    LIST_ITEMS("guishop.list.items", 2),
    ADD_ITEM("guishop.additem", 2),
    REMOVE_ITEM("guishop.removeitem", 2),
    CREATE_SHOP("guishop.create", 2),
    DELETE_SHOP("guishop.delete", 2),
    FORCE_SAVE("guishop.forcesave", 2),
    RELOAD("guishop.reload", 2);


    private final String permission;
    private final int defaultLevel;

    GuiShopPermission(String permission, int defaultLevel) {
        this.permission = permission;
        this.defaultLevel = defaultLevel;
    }

    @NotNull
    public Predicate<ServerCommandSource> require() {
        return Permissions.require(this.permission, this.defaultLevel);
    }
}
