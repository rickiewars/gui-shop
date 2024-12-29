package unsafedodo.guishop.economy;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.economy.services.ImpactorEconomyService;
import unsafedodo.guishop.economy.services.MockEconomyService;

public enum EconomyType {
    @SerializedName(value = "impactor", alternate = {"impactor-economy", "impactorEconomy"})
    IMPACTOR("impactor"),
    @SerializedName(value = "mock", alternate = {"test", "debug"})
    MOCK("mock");

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
                case IMPACTOR -> new ImpactorEconomyService();
                case MOCK -> new MockEconomyService();
            };
        } catch (ExceptionInInitializerError e) {
            GUIShop.LOGGER.error("Error initializing " + pretty() + ": " + e.getException().getMessage());
            return null;
        }
    }
}
