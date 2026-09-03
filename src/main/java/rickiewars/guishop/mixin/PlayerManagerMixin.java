package rickiewars.guishop.mixin;

import eu.pb4.common.economy.api.CommonEconomy;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void guishop_onPlayerConnectMixin(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        if (GUIShop.config.economyDisabled) {
            return;
        }

        DatabaseManager dm = GUIShop.databaseManager;
        String uuid = player.getUUID().toString();
        String name = player.getName().toString();

        CommonEconomy.getCurrencies(
            GUIShop.minecraftServer.getInstance()
        ).forEach(currency -> {
            dm.updateAccount(currency.id().toString(), uuid, name);
        });
    }
}