package rickiewars.guishop.util;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.database.DatabaseManager;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyCurrency;
import rickiewars.guishop.api.economy.impl.GuiShopEconomyProvider;
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
            Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "credits"),
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
        public Identifier currencyCreditsId = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "credits");
        /** Currency ID */
        public Identifier currencyCoinsId = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "coins");

        /** Account ID */
        public Identifier accountCardId = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "card");
        /** Account ID */
        public Identifier accountPouchId = Identifier.fromNamespaceAndPath(GuiShopEconomyProvider.ID, "pouch");
        public Map<String, GuiShopConfig.CurrencyDefinition> currencies = new HashMap<>();
        public Map<String, GuiShopConfig.AccountDefinition> accounts = new HashMap<>();
    }

    static public EconomyDetails initTestEconomy(DatabaseManager dbManager) {
        EconomyDetails details = new EconomyDetails();

        details.currencies.put(details.currencyCreditsId.getPath(), new GuiShopConfig.CurrencyDefinition(
            "Credits", "$", "", 2, GuiShopEconomyCurrency.DEFAULT_ICON_ID
        ));
        details.currencies.put(details.currencyCoinsId.getPath(), new GuiShopConfig.CurrencyDefinition(
            "Coins", "", " Coins", 0, Identifier.parse(CommonMethods.getItemId(Items.GOLD_NUGGET))
        ));

        details.accounts.put(details.accountCardId.getPath(), new GuiShopConfig.AccountDefinition(
            details.currencyCreditsId.getPath(),
            "Credit card",
            Identifier.parse(CommonMethods.getItemId(Items.PAPER))
        ));
        details.accounts.put(details.accountPouchId.getPath(), new GuiShopConfig.AccountDefinition(
            details.currencyCoinsId.getPath(),
            "Pouch",
            Identifier.parse(CommonMethods.getItemId(Items.BROWN_BUNDLE))
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
