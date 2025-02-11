package rickiewars.guishop.mixin;

import eu.pb4.common.economy.api.CommonEconomy;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.sql.DatabaseManager;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void guishop_onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData commonListenerCookie, CallbackInfo ci) {
        if (GUIShop.economyConfig.disabled) {
            return;
        }

        DatabaseManager dm = GUIShop.databaseManager;
        String uuid = player.getUuid().toString();
        String name = player.getName().toString();

        CommonEconomy.getCurrencies(player.server).forEach(currency -> {
            dm.addPlayer(currency.id().toString(), uuid, name);
        });
    }
}