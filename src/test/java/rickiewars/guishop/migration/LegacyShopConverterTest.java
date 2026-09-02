package rickiewars.guishop.migration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.serializer.SnbtShopStore;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the shops[] array of config/guishop.json -> one .snbt per shop under config/gui-shop/shops/.
 * Where possible the converted output is read back through the production SnbtShopStore rather than
 * string-matched, so these tests fail if either half of the round trip breaks.
 */
public class LegacyShopConverterTest extends MigrationTestBase {

    private boolean convert() {
        return LegacyShopConverter.convertIfNeeded(registries(), itemCodec());
    }

    private SnbtShopStore store() {
        return new SnbtShopStore(itemCodec(), shopsDir);
    }

    private Shop readShop(String id) {
        Optional<Shop> shop = store().readShop(shopsDir.resolve(id + ".snbt"));
        assertTrue(shop.isPresent(), "shop '" + id + "' did not decode");
        return shop.get();
    }

    private NbtCompound readRaw(String id) throws Exception {
        return StringNbtReader.readCompound(Files.readString(shopsDir.resolve(id + ".snbt"), StandardCharsets.UTF_8));
    }

    private List<String> shopFileNames() throws IOException {
        try (var files = Files.list(shopsDir)) {
            return files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".snbt")).sorted().toList();
        }
    }

    // --- the maintainer's real config ----------------------------------------------------------

    @Test
    void convertsTheRealWorldShopFileWithoutLosingAnything() throws Exception {
        installLegacyShops("guishop.json");

        assertTrue(convert());

        assertEquals(
            List.of("example_shop.snbt", "example_shop_2.snbt", "example_shop_3.snbt", "example_shop_4.snbt"),
            shopFileNames(),
            "the two shops both named 'Example Shop' must slugify to example_shop and example_shop_4, "
                + "because _2 and _3 are already taken by the slugs of 'Example Shop 2' and 'Example Shop 3'"
        );

        JsonArray legacyShops = JsonParser.parseString(readFixture("guishop.json"))
            .getAsJsonObject().getAsJsonArray("shops");

        Map<String, String> idByIndex = Map.of(
            "0", "example_shop", "1", "example_shop_2", "2", "example_shop_3", "3", "example_shop_4"
        );

        int totalItems = 0;
        for (int i = 0; i < legacyShops.size(); i++) {
            JsonObject legacy = legacyShops.get(i).getAsJsonObject();
            Shop shop = readShop(idByIndex.get(String.valueOf(i)));

            assertEquals(legacy.get("shopName").getAsString(), shop.getDisplayName());
            assertEquals(Identifier.of(legacy.get("icon").getAsString()), shop.iconId());

            JsonArray legacyItems = legacy.getAsJsonArray("items");
            assertEquals(legacyItems.size(), shop.getItems().size(), "item count changed for shop " + shop.getId());
            totalItems += legacyItems.size();

            for (int j = 0; j < legacyItems.size(); j++) {
                JsonObject legacyItem = legacyItems.get(j).getAsJsonObject();
                ShopItem item = shop.getItems().get(j);

                assertEquals(legacyItem.get("name").getAsString(), item.displayName());
                assertEquals(legacyItem.get("buyPrice").getAsLong(), item.buyPrice());
                assertEquals(legacyItem.get("sellPrice").getAsLong(), item.sellPrice());
                assertEquals(Identifier.of(legacyItem.get("itemId").getAsString()), item.itemId());
                assertEquals(
                    Identifier.of(legacyItem.get("currency").getAsString()),
                    item.explicitCurrencyId(),
                    "currency changed for '" + item.displayName() + "'"
                );
            }
        }

        assertEquals(104, totalItems, "the real config holds 104 listings across 4 shops");
    }

    @Test
    void stampsTheRunningGamesDataVersion() throws Exception {
        installLegacyShops("guishop.json");

        assertTrue(convert());

        int expected = SharedConstants.getGameVersion().dataVersion().id();
        assertEquals(Optional.of(expected), readRaw("example_shop").getInt("DataVersion"));
    }

    @Test
    void writesTheDoneMarkerAndClearsTheStagingDirectory() throws Exception {
        installLegacyShops("guishop.json");

        assertTrue(convert());

        assertTrue(Files.exists(doneMarker()), "without this marker file, every boot would re-convert the shops again");
        assertFalse(Files.exists(shopsDir.resolve(".converting")), "staging directory was left behind");
    }

    @Test
    void backsUpTheLegacyFileAndLeavesItInPlace() throws Exception {
        installLegacyShops("guishop.json");

        assertTrue(convert());

        assertTrue(Files.exists(backupOf(legacyShopFile)));
        assertEquals(readFixture("guishop.json"), Files.readString(backupOf(legacyShopFile), StandardCharsets.UTF_8));
        assertTrue(Files.exists(legacyShopFile), "deleting the legacy file is LegacyMigrationCleanup's job, not the converter's");
    }

    // --- field mapping -------------------------------------------------------------------------

    @Test
    void omitsEmptyDescriptionAndEmptyComponents() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Plain","icon":"minecraft:chest","items":[
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        String raw = Files.readString(shopsDir.resolve("plain.snbt"), StandardCharsets.UTF_8);
        assertFalse(raw.contains("description"), "an empty description must not be written");
        assertFalse(raw.contains("components"), "empty component changes must not be written");
        assertFalse(raw.contains("currency"), "an absent currency must not be written");
    }

    @Test
    void keepsDescriptionOrderAndOptionalFields() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Fancy","icon":"minecraft:diamond","defaultCurrency":"guishop:coins","items":[
              {"name":"Gem","itemId":"minecraft:diamond","description":["first","second","third"],
               "buyPrice":10,"sellPrice":4,"currency":"guishop:credit","components":{}}
            ]}]}
            """);

        assertTrue(convert());

        Shop shop = readShop("fancy");
        assertEquals("Fancy", shop.getDisplayName());
        assertEquals(Identifier.of("minecraft:diamond"), shop.iconId());
        assertTrue(shop.hasDefaultCurrency());
        assertEquals(Identifier.of("guishop:coins"), shop.getDefaultCurrencyId());

        ShopItem item = shop.getItems().getFirst();
        assertEquals(List.of("first", "second", "third"), item.description());
        assertEquals(10, item.buyPrice());
        assertEquals(4, item.sellPrice());
        assertEquals(Identifier.of("guishop:credit"), item.explicitCurrencyId());
    }

    @Test
    void defaultsAMissingIconToChest() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"No Icon","items":[
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        assertEquals(Identifier.ofVanilla("chest"), readShop("no_icon").iconId());
    }

    @Test
    void pricesSurviveAsLongs() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Big","items":[
              {"name":"Expensive","itemId":"minecraft:stone","description":[],
               "buyPrice":9007199254740993,"sellPrice":-1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        ShopItem item = readShop("big").getItems().getFirst();
        assertEquals(9007199254740993L, item.buyPrice(), "a price beyond 2^53 must not go through a double");
        assertEquals(-1, item.sellPrice(), "-1 means 'not sellable' and must round trip");
    }

    // --- components and enchantments -----------------------------------------------------------

    @Test
    void preservesEnchantmentsCustomNameLoreAndDamage() throws Exception {
        installLegacyShops("guishop-enchanted-current.json");

        assertTrue(convert());

        Shop shop = readShop("enchanted_shop");
        assertEquals(3, shop.getItems().size());

        ItemStack sword = shop.getItems().getFirst().stack();
        assertEquals(Items.NETHERITE_SWORD, sword.getItem());
        assertEquals(5, sword.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.SHARPNESS)));
        assertEquals(3, sword.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.UNBREAKING)));
        assertEquals("Excalibur", sword.get(DataComponentTypes.CUSTOM_NAME).getString());
        assertEquals(1, sword.get(DataComponentTypes.LORE).lines().size());
        assertEquals("Forged in fire", sword.get(DataComponentTypes.LORE).lines().getFirst().getString());
        assertEquals(42, sword.getDamage());

        ItemStack tagged = shop.getItems().get(1).stack();
        assertNotNull(tagged.get(DataComponentTypes.CUSTOM_DATA), "custom_data must survive the conversion");

        ItemStack plain = shop.getItems().get(2).stack();
        assertTrue(plain.getComponentChanges().isEmpty());
    }

    /**
     * The data-loss case this suite exists for.
     *
     * A config last written by the mod on 1.21.4 stores enchantments as
     * {"levels":{...},"show_in_tooltip":true}; 1.21.5 flattened that shape. Carrying it forward is
     * the DataFixer's job -- see LegacyShopConverter#decodeLegacyStack.
     */
    @Test
    void preservesEnchantmentsStoredInThePre1215Format() throws Exception {
        installLegacyShops("guishop-enchanted-1214.json");

        assertTrue(convert());

        Shop shop = readShop("old_enchanted_shop");
        ItemStack sword = shop.getItems().getFirst().stack();

        assertEquals(
            5,
            sword.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.SHARPNESS)),
            "pre-1.21.5 enchantment JSON must survive the migration"
        );
        assertNotNull(
            sword.get(DataComponentTypes.CUSTOM_NAME),
            "one unparseable component must not take the item's other components with it"
        );
    }

    /**
     * 1.21.5 unwrapped four components whose container shape changed: enchantments,
     * stored_enchantments, dyed_color and attribute_modifiers. All four must survive, and a
     * stale show_in_tooltip flag must not be fatal on its own either.
     */
    @Test
    void preservesEveryComponentShapeThat1215Unwrapped() throws Exception {
        installLegacyShops("guishop-legacy-shapes-1214.json");

        assertTrue(convert());

        Shop shop = readShop("legacy_shapes");
        assertEquals(5, shop.getItems().size());

        ItemStack sword = shop.getItems().get(0).stack();
        assertEquals(5, sword.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.SHARPNESS)));
        assertEquals(1, sword.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.MENDING)));

        ItemStack book = shop.getItems().get(1).stack();
        assertEquals(
            3,
            book.get(DataComponentTypes.STORED_ENCHANTMENTS).getLevel(registries().getEntryOrThrow(Enchantments.LURE)),
            "stored_enchantments was unwrapped by the same 1.21.5 change"
        );

        ItemStack chestplate = shop.getItems().get(2).stack();
        assertNotNull(chestplate.get(DataComponentTypes.DYED_COLOR), "dyed_color lost its rgb wrapper in 1.21.5");
        assertEquals(16711680, chestplate.get(DataComponentTypes.DYED_COLOR).rgb());

        ItemStack pickaxe = shop.getItems().get(3).stack();
        assertEquals(
            4,
            pickaxe.getEnchantments().getLevel(registries().getEntryOrThrow(Enchantments.EFFICIENCY)),
            "a flat enchantment map still carrying show_in_tooltip must not be discarded"
        );
    }

    /**
     * A component that no DataFixer can rescue -- here one from a mod that has since been removed --
     * must cost the item that component only, not its name as well.
     */
    @Test
    void anUnsalvageableComponentDoesNotTakeTheOthersWithIt() throws Exception {
        installLegacyShops("guishop-legacy-shapes-1214.json");

        assertTrue(convert());

        ItemStack stone = readShop("legacy_shapes").getItems().get(4).stack();
        assertEquals(Items.STONE, stone.getItem());
        assertNotNull(stone.get(DataComponentTypes.CUSTOM_NAME), "the readable component must be kept");
        assertEquals("Still Named", stone.get(DataComponentTypes.CUSTOM_NAME).getString());
    }

    /** The neighbouring items in a shop must not be affected by one item's component failure. */
    @Test
    void anUnparseableComponentDoesNotBreakTheRestOfTheShop() throws Exception {
        installLegacyShops("guishop-enchanted-1214.json");

        assertTrue(convert());

        Shop shop = readShop("old_enchanted_shop");
        assertEquals(2, shop.getItems().size());
        assertEquals("Plain Stone", shop.getItems().get(1).displayName());
        assertEquals(Items.STONE, shop.getItems().get(1).stack().getItem());
    }

    @Test
    void doesNothingWhenConversionWasAlreadyMarkedDone() throws Exception {
        installLegacyShops("guishop.json");
        markConversionComplete();

        assertTrue(convert());

        assertTrue(shopFileNames().isEmpty(), "seeing the done marker must stop a second conversion from running at all");
    }

    @Test
    void aSecondRunAfterASuccessfulOneChangesNothing() throws Exception {
        installLegacyShops("guishop.json");
        assertTrue(convert());
        Map<String, String> after = new HashMap<>();
        for (String name : shopFileNames()) {
            after.put(name, Files.readString(shopsDir.resolve(name), StandardCharsets.UTF_8));
        }

        assertTrue(convert());

        for (String name : shopFileNames()) {
            assertEquals(after.get(name), Files.readString(shopsDir.resolve(name), StandardCharsets.UTF_8));
        }
        assertEquals(after.keySet().size(), shopFileNames().size());
    }

    @Test
    void noLegacyFileIsANoOp() throws Exception {
        assertTrue(convert());
        assertFalse(Files.exists(shopsDir));
    }

    @Test
    void emptyShopsArrayWritesNothingAndNoDoneMarker() throws Exception {
        writeLegacyShops("{\"economyProviders\":{},\"shops\":[]}");

        assertTrue(convert());

        assertFalse(Files.exists(doneMarker()), "without a done marker, a later config that does have shops will still convert");
    }

    @Test
    void missingShopsKeyIsANoOp() throws Exception {
        writeLegacyShops("{\"economyProviders\":{}}");

        assertTrue(convert());

        assertFalse(Files.exists(doneMarker()));
    }

    // --- recovery ------------------------------------------------------------------------------

    @Test
    void skipsUnknownItemIdsAndKeepsTheRest() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Mixed","items":[
              {"name":"Gone","itemId":"someoldmod:removed_item","description":[],"buyPrice":1,"sellPrice":1,"components":{}},
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":2,"sellPrice":2,"components":{}}
            ]}]}
            """);

        assertTrue(convert(), "an item from an uninstalled mod must not fail the migration");

        Shop shop = readShop("mixed");
        assertEquals(1, shop.getItems().size());
        assertEquals("Rock", shop.getItems().getFirst().displayName());
    }

    @Test
    void wipesAStaleStagingDirectoryLeftByAnEarlierCrash() throws Exception {
        installLegacyShops("guishop.json");
        Path staging = shopsDir.resolve(".converting");
        Files.createDirectories(staging);
        Files.writeString(staging.resolve("half_written.snbt"), "{garbage", StandardCharsets.UTF_8);

        assertTrue(convert());

        assertFalse(Files.exists(shopsDir.resolve("half_written.snbt")), "debris from a crashed run must not reach shops/");
        assertEquals(4, shopFileNames().size());
    }

    @Test
    void aFailedConversionPublishesNoPartialShops() throws Exception {
        // the second shop has no shopName, which throws part-way through the loop
        writeLegacyShops("""
            {"shops":[
              {"shopName":"Good","items":[{"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}]},
              {"icon":"minecraft:chest","items":[]}
            ]}
            """);

        assertFalse(convert(), "a partial conversion is worse than none");

        assertTrue(shopFileNames().isEmpty(), "nothing may be published from a failed run");
        assertFalse(Files.exists(doneMarker()));
        assertTrue(Files.exists(legacyShopFile), "the legacy file must survive so the next boot can retry");
        assertTrue(Files.exists(backupOf(legacyShopFile)));
    }

    @Test
    void aFailedRunCanBeRetriedAfterTheConfigIsFixed() throws Exception {
        writeLegacyShops("""
            {"shops":[{"icon":"minecraft:chest","items":[]}]}
            """);
        assertFalse(convert());

        writeLegacyShops("""
            {"shops":[{"shopName":"Fixed","items":[
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert(), "the staging dir left by the failed run must not block the retry");
        assertEquals(List.of("fixed.snbt"), shopFileNames());
    }

    /**
     * An unreadable legacy file must be reported as a failed conversion, so the mod logs the error
     * and skips loading shops for the boot. convertIfNeeded parses outside its main try block and
     * catches only IOException, so the unchecked JsonSyntaxException escapes instead.
     */
    @Test
    void unreadableLegacyFileFailsClosed() throws Exception {
        writeLegacyShops("{\"shops\":[{\"shopName\":\"Broken\",");

        boolean ok = assertDoesNotThrow(this::convert, "a corrupt guishop.json must return false, not escape as an unchecked exception");
        assertFalse(ok);
        assertFalse(Files.exists(doneMarker()));
    }

    // --- SNBT escaping -------------------------------------------------------------------------

    @Test
    void escapesQuotesAndBackslashesInNames() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"The \\"Real\\" C:\\\\Shop","items":[
              {"name":"A \\"quoted\\" rock","itemId":"minecraft:stone","description":["back\\\\slash"],
               "buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        Shop shop = readShop(shopFileNames().getFirst().replace(".snbt", ""));
        assertEquals("The \"Real\" C:\\Shop", shop.getDisplayName());
        assertEquals("A \"quoted\" rock", shop.getItems().getFirst().displayName());
        assertEquals(List.of("back\\slash"), shop.getItems().getFirst().description());
    }

    /**
     * quote() escapes only \\ and ", but Minecraft's SNBT reader accepts a raw newline or tab inside
     * a quoted string, so names carrying control characters still round trip.
     */
    @Test
    void namesContainingControlCharactersStillRoundTrip() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Line one\\nLine two","items":[
              {"name":"Tabbed\\trock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        Shop shop = readShop(shopFileNames().getFirst().replace(".snbt", ""));
        assertEquals("Line one\nLine two", shop.getDisplayName());
        assertEquals("Tabbed\trock", shop.getItems().getFirst().displayName());
    }

    // --- hand-edited files ---------------------------------------------------------------------
    //
    // The converter's contract is fail-closed: anything it cannot make sense of aborts the whole
    // run rather than publishing a partial set (see the atomicity tests above). That applies to
    // genuine corruption only -- a field that has an obvious reading must be read, not treated as
    // corruption.

    @Test
    void shopWithoutAnItemsArrayConvertsAsAnEmptyShop() throws Exception {
        writeLegacyShops("""
            {"shops":[
              {"shopName":"Good","items":[{"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}]},
              {"shopName":"No Items"}
            ]}
            """);

        assertTrue(convert(), "an absent items array reads as an empty shop, not as corruption");

        assertEquals(List.of("good.snbt", "no_items.snbt"), shopFileNames());
        assertEquals(1, readShop("good").getItems().size());
        assertTrue(readShop("no_items").getItems().isEmpty());
    }

    /**
     * An item with no components key at all passes null into ComponentChanges.CODEC. That is
     * tolerated -- the codec reports a parse error and the item converts as a plain stack.
     */
    @Test
    void itemWithoutComponentsKeyStillConverts() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"No Components","items":[
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1}
            ]}]}
            """);

        assertTrue(convert());

        ShopItem item = readShop("no_components").getItems().getFirst();
        assertEquals(Items.STONE, item.stack().getItem());
        assertTrue(item.stack().getComponentChanges().isEmpty());
    }

    /**
     * LegacyConfigMigrator#readIcon already falls back to a default when a legacy icon is not a valid
     * identifier. The converter must handle a shop icon the same way -- an icon is decoration, and
     * losing every shop over one is not a proportionate response.
     */
    @Test
    void malformedShopIconFallsBackToChest() throws Exception {
        writeLegacyShops("""
            {"shops":[{"shopName":"Bad Icon","icon":"Not An Id!","items":[
              {"name":"Rock","itemId":"minecraft:stone","description":[],"buyPrice":1,"sellPrice":1,"components":{}}
            ]}]}
            """);

        assertTrue(convert());

        Shop shop = readShop("bad_icon");
        assertEquals(Identifier.ofVanilla("chest"), shop.iconId());
        assertEquals(1, shop.getItems().size(), "the listings must survive a bad icon");
    }
}
