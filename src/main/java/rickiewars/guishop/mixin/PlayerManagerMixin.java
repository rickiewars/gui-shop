package rickiewars.guishop.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.economy.EconomyUtils;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void guishop_onPlayerConnectMixin(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        EconomyUtils.registerAccounts(
            GUIShop.databaseManager,
            EconomyUtils.getCurrencies(GUIShop.minecraftServer.getInstance()),
            player.getUUID().toString(),
            player.getName().toString()
        );
    }
}