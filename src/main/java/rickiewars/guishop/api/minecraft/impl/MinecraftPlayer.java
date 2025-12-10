package rickiewars.guishop.api.minecraft.impl;

import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rickiewars.guishop.api.minecraft.IInventory;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.economy.EconomyUtils;

public class MinecraftPlayer implements IPlayer {
    private final ServerPlayerEntity player;

    public MinecraftPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public IInventory getInventory() {
        return new MinecraftInventory(player.getInventory());
    }

    @Override
    public EconomyAccount getAccount(Identifier currencyId) {
        return EconomyUtils.getDefaultAccount(player, currencyId);
    }

    @Override
    public void sendMessage(Text message) {
        player.sendMessage(message);
    }

    @Override
    public String getUuid() {
        return player.getUuidAsString();
    }
}
