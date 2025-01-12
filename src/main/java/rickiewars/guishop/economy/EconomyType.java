package rickiewars.guishop.economy;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.economy.services.DiamondEconomyService;
import rickiewars.guishop.economy.services.MockEconomyService;

public enum EconomyType {
//    @SerializedName(value = "impactor", alternate = {"impactor-economy", "impactorEconomy"})
//    IMPACTOR("impactor"),
    @SerializedName(value = "mock", alternate = {"test", "debug"})
    MOCK("mock"),
    @SerializedName(value = "diamond", alternate = {"diamond-economy", "diamondEconomy"})
    DIAMOND("diamondeconomy");

    private final String modId;

    EconomyType(String modId) {
        this.modId = modId;
    }

    public boolean modIsLoaded() {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public String pretty() {
        return this.name().substring(0, 1).toUpperCase()
                + this.name().substring(1).toLowerCase();
    }

    public static EconomyType firstLoaded() {
        for (EconomyType economyType : EconomyType.values()) {
            if (economyType.modIsLoaded()) {
                return economyType;
            }
        }
        return null;
    }

    public IEconomyService getEconomyService() {
        try {
            return switch (this) {
//                case IMPACTOR -> new ImpactorEconomyService();
                case DIAMOND -> new DiamondEconomyService();
                case MOCK -> new MockEconomyService();
            };
        } catch (ExceptionInInitializerError e) {
            GUIShop.LOGGER.error("Error initializing " + pretty() + ": " + e.getException().getMessage());
            return null;
        }
    }

    public static EconomyType getTypeFromService(IEconomyService service) {
        /*if (service instanceof ImpactorEconomyService) {
            return IMPACTOR;
        } else*/ if (service instanceof MockEconomyService) {
            return MOCK;
        } else if (service instanceof DiamondEconomyService) {
            return DIAMOND;
        }
        else {
            return null;
        }
    }
}
