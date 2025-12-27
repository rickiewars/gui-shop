package rickiewars.guishop.util;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.impl.TestServer;
import rickiewars.guishop.config.EconomyConfig;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {

    static public Shop testShop(int itemCount) {
        return testShop(
            List.of("minecraft:stone"),
            Identifier.of(GuiShopEconomyProvider.ID, "credits"),
            itemCount
        );
    }

    static public Shop testShop(Identifier currencyId, int itemCount) {
        return testShop(
            List.of("minecraft:stone"),
            currencyId,
            itemCount
        );
    }

    static public Shop testShop(List<String> itemIdSequence, Identifier currencyId, int itemCount) {
        List<ShopItem> shopItems = new ArrayList<>();
        int sequenceLength = itemIdSequence.size();

        for (int i = 0; i < itemCount; i++) {
            shopItems.add(new ShopItem(
                "Item " + (i + 1),
                itemIdSequence.get(i % sequenceLength),
                10,
                10,
                currencyId,
                new String[0],
                null
            ));
        }
        return new Shop("Test Shop", shopItems, currencyId);
    }

    static public class EconomyDetails {
        /** Currency ID */
        public Identifier currencyCreditsId = Identifier.of(GuiShopEconomyProvider.ID, "credits");
        /** Currency ID */
        public Identifier currencyCoinsId = Identifier.of(GuiShopEconomyProvider.ID, "coins");

        /** Account ID */
        public Identifier accountCardId = Identifier.of(GuiShopEconomyProvider.ID, "card");
        /** Account ID */
        public Identifier accountPouchId = Identifier.of(GuiShopEconomyProvider.ID, "pouch");
        public Map<String, EconomyConfig.CurrencyDefinition> currencies = new HashMap<>();
        public Map<String, EconomyConfig.AccountDefinition> accounts = new HashMap<>();
    }

    static public EconomyDetails initTestEconomy(DatabaseManager dbManager) {
        EconomyDetails details = new EconomyDetails();

        details.currencies.put(details.currencyCreditsId.getPath(), new EconomyConfig.CurrencyDefinition(
            "Credits", "$", "", 2, new ItemStack(GuiShopEconomyCurrency.DEFAULT_ICON)
        ));
        details.currencies.put(details.currencyCoinsId.getPath(), new EconomyConfig.CurrencyDefinition(
            "Coins", "", " Coins", 0, new ItemStack(Items.GOLD_NUGGET)
        ));

        details.accounts.put(details.accountCardId.getPath(), new EconomyConfig.AccountDefinition(
            details.currencyCreditsId.getPath(),
            "Credit card",
            new ItemStack(Items.PAPER)
        ));
        details.accounts.put(details.accountPouchId.getPath(), new EconomyConfig.AccountDefinition(
            details.currencyCoinsId.getPath(),
            "Pouch",
            new ItemStack(Items.BROWN_BUNDLE)
        ));

        GUIShop.economyConfig = new EconomyConfig();
        GUIShop.economyConfig.economy = new EconomyConfig.EconomyProviderDefinition(details.currencies, details.accounts);
        GUIShop.databaseManager = dbManager;
        GUIShop.minecraftServer = new TestServer();

        return details;
    }

    static public ItemEnchantmentsComponent buildEnchantmentsComponent(Map<RegistryKey<Enchantment>, Integer> enchantments) {
        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        var lookup = BuiltinRegistries.createWrapperLookup();

        enchantments.forEach((enchantment, level) -> {
            builder.add(lookup.getEntryOrThrow(enchantment), level);
        });

        return builder.build();
    }

}
