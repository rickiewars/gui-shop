package rickiewars.guishop.serializer;

import net.minecraft.data.registries.VanillaRegistries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rickiewars.guishop.MinecraftTest;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.shop.Shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SnbtGoldenFixturesTest extends MinecraftTest {

    private static final Path FIXTURES_DIR = Path.of("src/test/resources/shops");

    @TempDir
    Path tempDir;

    private SnbtShopStore store;

    /// Fixtures are pinned at the oldest supported data version, exercising the real DataFixer
    /// upgrade path. Copied into tempDir since readShop() rewrites old files in place.
    @BeforeEach
    void setup() throws IOException {
        MinecraftItemCodec codec = new MinecraftItemCodec(VanillaRegistries.createLookup());
        for (Path source : snbtFilesIn(FIXTURES_DIR)) {
            Files.copy(source, tempDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        store = new SnbtShopStore(codec, tempDir);
    }

    @Test
    void everyFixtureDecodesWithComponentsIntact() throws IOException {
        for (Path fixture : snbtFilesIn(tempDir)) {
            Optional<Shop> shop = store.readShop(fixture);

            assertTrue(shop.isPresent(), "fixture " + fixture + " failed to decode");
            assertEquals(1, shop.get().getItems().size(), "fixture " + fixture + " should have exactly one entry");
            assertFalse(shop.get().getItems().get(0).stack().isEmpty(), "fixture " + fixture + " decoded to an empty stack");
        }
    }

    private static List<Path> snbtFilesIn(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> fixtures = files.filter(p -> p.toString().endsWith(".snbt")).toList();
            assertFalse(fixtures.isEmpty(), "no golden fixtures found in " + dir);
            return fixtures;
        }
    }
}
