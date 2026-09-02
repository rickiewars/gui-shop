package rickiewars.guishop.factories;

import rickiewars.guishop.shop.SellPricing;

public class SellPricingFactory {

    private double firstUsePenalty = 0.37;
    private double minValueFraction = 0;
    private double damageCurveExponent = 9.0;
    private double repairCostPenaltyPerPoint = 0.37;
    private double customNamePenalty = 0.37;
    private double lorePenalty = 0.37;

    public SellPricingFactory withFirstUsePenalty(double v) {
        this.firstUsePenalty = v;
        return this;
    }

    public SellPricingFactory withMinValueFraction(double v) {
        this.minValueFraction = v;
        return this;
    }

    public SellPricingFactory withDamageCurveExponent(double v) {
        this.damageCurveExponent = v;
        return this;
    }

    public SellPricingFactory withRepairCostPenaltyPerPoint(double v) {
        this.repairCostPenaltyPerPoint = v;
        return this;
    }

    public SellPricingFactory withCustomNamePenalty(double v) {
        this.customNamePenalty = v;
        return this;
    }

    public SellPricingFactory withLorePenalty(double v) {
        this.lorePenalty = v;
        return this;
    }

    public SellPricing build() {
        return new SellPricing(firstUsePenalty, minValueFraction, damageCurveExponent, repairCostPenaltyPerPoint, customNamePenalty, lorePenalty);
    }
}
