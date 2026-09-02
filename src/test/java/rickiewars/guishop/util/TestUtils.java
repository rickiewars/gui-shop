package rickiewars.guishop.util;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.impl.TestServer;
import rickiewars.guishop.config.GuiShopConfig;
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

    static public Shop testShop(String name, Identifier currencyId, int itemCount) {
        return testShop(
            name,
            List.of("minecraft:stone"),
            currencyId,
            itemCount
        );
    }

    static public Shop testShop(List<String> itemIdSequence, Identifier currencyId, int itemCount) {
        return testShop("Test Shop", itemIdSequence, currencyId, itemCount);
    }

    static public Shop testShop(String name, List<String> itemIdSequence, Identifier currencyId, int itemCount) {
        List<ShopItem> shopItems = new ArrayList<>();
        int sequenceLength = itemIdSequence.size();

        for (int i = 0; i < itemCount; i++) {
            String itemId = itemIdSequence.get(i % sequenceLength);
            ItemStack stack = new ItemStack(CommonMethods.getItem(itemId));
            shopItems.add(new ShopItem(
                "Item " + (i + 1),
                stack,
                10,
                10,
                currencyId,
                List.of()
            ));
        }
        return new Shop(CommonMethods.slugify(name, java.util.Set.of()), name, shopItems, currencyId);
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
        public Map<String, GuiShopConfig.CurrencyDefinition> currencies = new HashMap<>();
        public Map<String, GuiShopConfig.AccountDefinition> accounts = new HashMap<>();
    }

    static public EconomyDetails initTestEconomy(DatabaseManager dbManager) {
        EconomyDetails details = new EconomyDetails();

        details.currencies.put(details.currencyCreditsId.getPath(), new GuiShopConfig.CurrencyDefinition(
            "Credits", "$", "", 2, GuiShopEconomyCurrency.DEFAULT_ICON_ID
        ));
        details.currencies.put(details.currencyCoinsId.getPath(), new GuiShopConfig.CurrencyDefinition(
            "Coins", "", " Coins", 0, Identifier.of(CommonMethods.getItemId(Items.GOLD_NUGGET))
        ));

        details.accounts.put(details.accountCardId.getPath(), new GuiShopConfig.AccountDefinition(
            details.currencyCreditsId.getPath(),
            "Credit card",
            Identifier.of(CommonMethods.getItemId(Items.PAPER))
        ));
        details.accounts.put(details.accountPouchId.getPath(), new GuiShopConfig.AccountDefinition(
            details.currencyCoinsId.getPath(),
            "Pouch",
            Identifier.of(CommonMethods.getItemId(Items.BROWN_BUNDLE))
        ));

        GUIShop.config = new GuiShopConfig();
        GUIShop.config.economy = new GuiShopConfig.EconomyProviderDefinition(details.currencies, details.accounts);
        GUIShop.databaseManager = dbManager;
        GUIShop.minecraftServer = new TestServer();

        return details;
    }

    static public ItemEnchantmentsComponent buildEnchantmentsComponent(Map<RegistryKey<Enchantment>, Integer> enchantments, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup) {
        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        enchantments.forEach((enchantment, level) -> {
            builder.add(lookup.getEntryOrThrow(enchantment), level);
        });

        return builder.build();
    }

}
