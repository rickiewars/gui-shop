package rickiewars.guishop.serializer;

import net.minecraft.data.registries.VanillaRegistries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.VanillaItemCodec;
import rickiewars.guishop.shop.Shop;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SnbtGoldenFixturesTest extends MinecraftTest {

    private static final Path FIXTURE_DIR = Path.of("src/test/resources/shops/4671");

    private SnbtShopStore store;

    @BeforeEach
    void setup() {
        VanillaItemCodec codec = new VanillaItemCodec(VanillaRegistries.createLookup());
        store = new SnbtShopStore(codec, FIXTURE_DIR);
    }

    private static final List<String> FIXTURES = List.of(
        "enchanted_sword.snbt",
        "written_book.snbt",
        "shulker_box.snbt",
        "custom_named_potion.snbt",
        "custom_data_item.snbt",
        "plain_item.snbt"
    );

    @Test
    void everyFixtureDecodesWithComponentsIntact() {
        for (String filename : FIXTURES) {
            Optional<Shop> shop = store.readShop(FIXTURE_DIR.resolve(filename));

            assertTrue(shop.isPresent(), "fixture " + filename + " failed to decode");
            assertEquals(1, shop.get().getItems().size(), "fixture " + filename + " should have exactly one entry");
            assertFalse(shop.get().getItems().get(0).stack().isEmpty(), "fixture " + filename + " decoded to an empty stack");
        }
    }
}
