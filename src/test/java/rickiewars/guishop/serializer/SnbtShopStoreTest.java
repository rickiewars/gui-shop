package rickiewars.guishop.serializer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.TestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SnbtShopStoreTest extends MinecraftTest {

    @TempDir
    Path tempDir;

    private MinecraftItemCodec itemCodec;
    private SnbtShopStore store;

    private net.minecraft.core.HolderLookup.Provider registryLookup;

    @BeforeEach
    void setup() {
        registryLookup = VanillaRegistries.createLookup();
        itemCodec = new MinecraftItemCodec(registryLookup);
        store = new SnbtShopStore(itemCodec, tempDir);
    }

    @Test
    void writeThenReadRoundTripsEnchantedItem() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.ENCHANTMENTS, TestUtils.buildEnchantmentsComponent(
            Map.of(Enchantments.SHARPNESS, 5), registryLookup
        ));

        ShopItem item = new ShopItem("Sharp Sword", new MinecraftItemStack(sword), 50000, 25000, null, List.of("Freshly ground"));
        Shop shop = new Shop("enchanted_gear", "Enchanted Gear", List.of(item), null, ResourceId.ofVanilla("enchanting_table"));

        store.writeShop(shop);
        List<Shop> loaded = store.readAll();

        assertEquals(1, loaded.size());
        Shop result = loaded.get(0);
        assertEquals("enchanted_gear", result.getId());
        assertEquals("Enchanted Gear", result.getDisplayName());
        assertEquals(1, result.getItems().size());
        assertEquals(item, result.getItems().get(0));
    }

    @Test
    void corruptFileIsSkippedButOthersStillLoad() throws IOException {
        ShopItem item = new ShopItem("Item", new MinecraftItemStack(new ItemStack(Items.STONE)), 10, 5, null, List.of());
        Shop goodShop = new Shop("good_shop", "Good Shop", List.of(item), null);
        store.writeShop(goodShop);

        Files.writeString(tempDir.resolve("corrupt.snbt"), "{this is not valid snbt!!", StandardCharsets.UTF_8);

        List<Shop> loaded = store.readAll();

        assertEquals(1, loaded.size());
        assertEquals("good_shop", loaded.get(0).getId());
    }

    @Test
    void missingDataVersionTreatedAsCorrupt() throws IOException {
        Files.writeString(tempDir.resolve("no_version.snbt"), "{displayName:\"X\",entries:[]}", StandardCharsets.UTF_8);

        Optional<Shop> result = store.readShop(tempDir.resolve("no_version.snbt"));
        assertTrue(result.isEmpty());
    }

    @Test
    void newerDataVersionIsSkippedAndFileNotRewritten() throws IOException {
        String content = "{DataVersion:999999999,displayName:\"X\",entries:[]}";
        Path file = tempDir.resolve("future.snbt");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        Optional<Shop> result = store.readShop(file);

        assertTrue(result.isEmpty());
        assertEquals(content, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void countKeyOnStackIsIgnoredButLogsWarning() {
        var stack = new ItemStack(Items.STONE);
        ShopItem item = new ShopItem("Stack item", new MinecraftItemStack(stack), 10, 5, null, List.of());
        Shop shop = new Shop("shop", "Shop", List.of(item), null);
        store.writeShop(shop);

        // Manually inject a count key into the written stack compound to simulate a hand-edited file
        Path file = tempDir.resolve("shop.snbt");
        assertDoesNotThrow(() -> {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String withCount = content.replaceFirst("stack:\\{", "stack:{count:5,");
            Files.writeString(file, withCount, StandardCharsets.UTF_8);
        });

        List<Shop> loaded = store.readAll();
        assertEquals(1, loaded.size());
        assertEquals(1, loaded.get(0).getItems().size());
    }

    @Test
    void missingShopsDirReturnsEmptyList() {
        SnbtShopStore emptyStore = new SnbtShopStore(itemCodec, tempDir.resolve("does_not_exist"));
        assertTrue(emptyStore.readAll().isEmpty());
    }

    @Test
    void upgradingOldDataVersionBacksUpOriginalBeforeRewriting() throws IOException {
        ShopItem item = new ShopItem("Item", new MinecraftItemStack(new ItemStack(Items.STONE)), 10, 5, null, List.of());
        Shop shop = new Shop("old_shop", "Old Shop", List.of(item), null);
        store.writeShop(shop);

        Path file = tempDir.resolve("old_shop.snbt");
        int oldVersion = itemCodec.currentDataVersion() - 1;
        String original = Files.readString(file, StandardCharsets.UTF_8)
            .replaceFirst("DataVersion:\\d+", "DataVersion:" + oldVersion);
        Files.writeString(file, original, StandardCharsets.UTF_8);

        Optional<Shop> result = store.readShop(file);
        assertTrue(result.isPresent());

        // the live file should now be rewritten at the current DataVersion
        String rewritten = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(rewritten.contains("DataVersion:" + itemCodec.currentDataVersion()));

        // a backup of the pre-upgrade file should exist, containing the old DataVersion untouched
        Path backupsDir = tempDir.resolve("backups");
        assertTrue(Files.isDirectory(backupsDir));
        List<Path> backups = Files.list(backupsDir)
            .filter(p -> p.getFileName().toString().startsWith("old_shop-" + oldVersion + "-"))
            .toList();
        assertEquals(1, backups.size());
        assertEquals(original, Files.readString(backups.get(0), StandardCharsets.UTF_8));
    }

    @Test
    void rewriteIsSkippedWhenBackupFails() throws IOException {
        ShopItem item = new ShopItem("Item", new MinecraftItemStack(new ItemStack(Items.STONE)), 10, 5, null, List.of());
        Shop shop = new Shop("old_shop", "Old Shop", List.of(item), null);
        store.writeShop(shop);

        Path file = tempDir.resolve("old_shop.snbt");
        int oldVersion = itemCodec.currentDataVersion() - 1;
        String original = Files.readString(file, StandardCharsets.UTF_8)
            .replaceFirst("DataVersion:\\d+", "DataVersion:" + oldVersion);
        Files.writeString(file, original, StandardCharsets.UTF_8);

        // block backup creation by occupying the backups directory path with a plain file
        Files.writeString(tempDir.resolve("backups"), "blocked", StandardCharsets.UTF_8);

        Optional<Shop> result = store.readShop(file);

        // decoding still succeeds even though the rewrite is skipped
        assertTrue(result.isPresent());
        assertEquals(original, Files.readString(file, StandardCharsets.UTF_8));
    }
}
