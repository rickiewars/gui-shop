package rickiewars.guishop.economy.services;

import com.gmail.sneakdevs.diamondeconomy.integration.DiamondAccount;
import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.server.network.ServerPlayerEntity;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.economy.IEconomyService;
import com.gmail.sneakdevs.diamondeconomy.integration.DiamondEconomyProvider;
import rickiewars.guishop.util.ServerHandler;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DiamondEconomyService implements IEconomyService {
    public DiamondEconomyProvider getProvider() {
        // TODO: Diamond economy should register itself at CommonEconomy using the DiamondEconomyProvider.init() method
        //       Once it is registered, the provider can be retrieved using CommonEconomy.getProvider("<modid>")
        //       Potential other currency mods should register themselves in the same way.
        //       Then this IEconomyService abstraction can be replaced with the CommonEconomy API
        return DiamondEconomyProvider.INSTANCE;
    }

    private EconomyAccount getAccount(UUID uuid) {
        ServerPlayerEntity player = ServerHandler.getPlayerByUUID(uuid);
        if (player == null) {
            GUIShop.LOGGER.error("Player not found for UUID: " + uuid);
            return null;
        }
        GameProfile profile = player.getGameProfile();
        return getProvider().getAccount(null, profile, DiamondAccount.ID.getPath());
    }

    @Override
    public boolean add(UUID uuid, long amount) {
        EconomyAccount account = getAccount(uuid);
        if (account == null) return false;

        return account.increaseBalance(amount).isSuccessful();
    }

    @Override
    public boolean remove(UUID uuid, long amount) {
        EconomyAccount account = getAccount(uuid);
        if (account == null) return false;

        return account.decreaseBalance(amount).isSuccessful();
    }

    @Override
    public boolean hasEnoughMoney(UUID uuid, long amount) throws ExecutionException, InterruptedException {
        EconomyAccount account = getAccount(uuid);
        if (account == null) throw new ExecutionException(new Throwable(
                "Account not found for UUID: " + uuid.toString()
        ));

        return account.canDecreaseBalance(amount).isSuccessful();
    }

    @Override
    public long getBalance(UUID uuid) throws ExecutionException, InterruptedException {
        EconomyAccount account = getAccount(uuid);
        if (account == null) throw new ExecutionException(new Throwable(
                "Account not found for UUID: " + uuid.toString()
        ));

        return account.balance();
    }

    @Override
    public boolean transfer(UUID sender, UUID receiver, long amount) {
        EconomyAccount senderAccount = getAccount(sender);
        EconomyAccount receiverAccount = getAccount(receiver);
        if (senderAccount == null || receiverAccount == null) return false;

        if (senderAccount.canDecreaseBalance(amount).isFailure()) return false;
        if (receiverAccount.canIncreaseBalance(amount).isFailure()) return false;
        return senderAccount.decreaseBalance(amount).isSuccessful()
                && receiverAccount.increaseBalance(amount).isSuccessful();
    }
}
