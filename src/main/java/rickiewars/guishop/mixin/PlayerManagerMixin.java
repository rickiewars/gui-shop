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
        GUIShop.LOGGER.info("Player connected");
        DatabaseManager dm = GUIShop.databaseManager;
        String uuid = player.getUuid().toString();
        String name = player.getName().toString();
        GUIShop.LOGGER.info("Initializing account for player: " + uuid + " " + name);

        CommonEconomy.getCurrencies(player.server).forEach(currency -> {
            GUIShop.LOGGER.info("Adding player to currency: " + currency.id().toString());
            dm.addPlayer(currency.id().toString(), uuid, name);
        });
    }
}