package rickiewars.guishop.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
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
            ResourceId.of(GuiShopEconomyProvider.ID, "credits"),
            itemCount
        );
    }

    static public Shop testShop(ResourceId currencyId, int itemCount) {
        return testShop(
            List.of("minecraft:stone"),
            currencyId,
            itemCount
        );
    }

    static public Shop testShop(String name, ResourceId currencyId, int itemCount) {
        return testShop(
            name,
            List.of("minecraft:stone"),
            currencyId,
            itemCount
        );
    }

    static public Shop testShop(List<String> itemIdSequence, ResourceId currencyId, int itemCount) {
        return testShop("Test Shop", itemIdSequence, currencyId, itemCount);
    }

    static public Shop testShop(String name, List<String> itemIdSequence, ResourceId currencyId, int itemCount) {
        List<ShopItem> shopItems = new ArrayList<>();
        int sequenceLength = itemIdSequence.size();

        for (int i = 0; i < itemCount; i++) {
            String itemId = itemIdSequence.get(i % sequenceLength);
            ItemStack stack = new ItemStack(CommonMethods.getItem(itemId));
            shopItems.add(new ShopItem(
                "Item " + (i + 1),
                new MinecraftItemStack(stack),
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
        public ResourceId currencyCreditsId = ResourceId.of(GuiShopEconomyProvider.ID, "credits");
        /** Currency ID */
        public ResourceId currencyCoinsId = ResourceId.of(GuiShopEconomyProvider.ID, "coins");

        /** Account ID */
        public ResourceId accountCardId = ResourceId.of(GuiShopEconomyProvider.ID, "card");
        /** Account ID */
        public ResourceId accountPouchId = ResourceId.of(GuiShopEconomyProvider.ID, "pouch");
        public Map<String, GuiShopConfig.CurrencyDefinition> currencies = new HashMap<>();
        public Map<String, GuiShopConfig.AccountDefinition> accounts = new HashMap<>();
    }

    static public EconomyDetails initTestEconomy(DatabaseManager dbManager) {
        EconomyDetails details = new EconomyDetails();

        details.currencies.put(details.currencyCreditsId.path(), new GuiShopConfig.CurrencyDefinition(
            "Credits", "$", "", 2, GuiShopEconomyCurrency.DEFAULT_ICON_ID
        ));
        details.currencies.put(details.currencyCoinsId.path(), new GuiShopConfig.CurrencyDefinition(
            "Coins", "", " Coins", 0, ResourceId.parse(CommonMethods.getItemId(Items.GOLD_NUGGET))
        ));

        details.accounts.put(details.accountCardId.path(), new GuiShopConfig.AccountDefinition(
            details.currencyCreditsId.path(),
            "Credit card",
            ResourceId.parse(CommonMethods.getItemId(Items.PAPER))
        ));
        details.accounts.put(details.accountPouchId.path(), new GuiShopConfig.AccountDefinition(
            details.currencyCoinsId.path(),
            "Pouch",
            ResourceId.parse(CommonMethods.getItemId(Items.BROWN_BUNDLE))
        ));

        GUIShop.config = new GuiShopConfig();
        GUIShop.config.economy = new GuiShopConfig.EconomyProviderDefinition(details.currencies, details.accounts);
        GUIShop.databaseManager = dbManager;
        GUIShop.minecraftServer = new TestServer();

        return details;
    }

    static public ItemEnchantments buildEnchantmentsComponent(Map<ResourceKey<Enchantment>, Integer> enchantments, net.minecraft.core.HolderLookup.Provider lookup) {
        var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        enchantments.forEach((enchantment, level) -> {
            builder.upgrade(lookup.getOrThrow(enchantment), level);
        });

        return builder.toImmutable();
    }

}
