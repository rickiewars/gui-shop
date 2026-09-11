package rickiewars.guishop.migration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.ItemRegistry;
import rickiewars.guishop.api.minecraft.impl.VanillaItemCodec;
import rickiewars.guishop.config.ConfigManager;
import rickiewars.guishop.util.CommonMethods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class LegacyShopConverter {
    private static final String CONVERSION_DONE_MARKER = ".conversion-complete";
    private static final String STAGING_DIR_NAME = ".converting";

    /// Legacy configs carry no version field, so the DataFixer needs an assumed starting point.
    /// 1.20.5 is the first release with item components at all, so nothing older could have written
    /// the components block this converter reads. Assuming too LOW is harmless -- the fixes in
    /// between simply do not match -- while assuming too HIGH silently skips the fixes that matter.
    private static final int LEGACY_FLOOR_DATA_VERSION = 3837;

    private LegacyShopConverter() {}

    public static boolean convertIfNeeded(MinecraftServer server, VanillaItemCodec itemCodec) {
        return convertIfNeeded(server.registryAccess(), itemCodec);
    }

    public static boolean convertIfNeeded(HolderLookup.Provider registries, VanillaItemCodec itemCodec) {
        Path legacyShopFile = ConfigManager.configRoot().resolve("guishop.json");
        Path shopsDir = ConfigManager.shopsDir();
        Path doneMarker = shopsDir.resolve(CONVERSION_DONE_MARKER);

        if (Files.exists(doneMarker)) return true;
        if (!Files.exists(legacyShopFile)) return true;

        JsonObject root;
        try {
            root = JsonParser.parseString(Files.readString(legacyShopFile, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            // readString throws IOException, but malformed JSON throws unchecked -- catch both so a
            // corrupt legacy file is a failed conversion rather than a failed server start.
            GUIShop.LOGGER.error("Could not read legacy shop config for conversion: {}", e.getMessage());
            return false;
        }

        if (!root.has("shops") || root.getAsJsonArray("shops").isEmpty()) return true;

        GUIShop.LOGGER.info("Converting legacy shops from {} into {}", legacyShopFile, shopsDir);

        Path staging = shopsDir.resolve(STAGING_DIR_NAME);
        try {
            MigrationBackup.backup(legacyShopFile);

            if (Files.exists(staging)) {
                deleteRecursively(staging);
            }
            Files.createDirectories(staging);

            Set<String> usedIds = new HashSet<>();
            for (var shopElement : root.getAsJsonArray("shops")) {
                convertShop(shopElement.getAsJsonObject(), registries, itemCodec, staging, usedIds);
            }

            Files.createDirectories(shopsDir);
            for (Path file : listFiles(staging)) {
                Files.move(file, shopsDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(staging);

            Files.writeString(doneMarker, "", StandardCharsets.UTF_8);
            GUIShop.LOGGER.info("Legacy shop conversion complete");
            return true;
        } catch (Exception e) {
            GUIShop.LOGGER.error("Legacy shop conversion failed, shop system will not start this boot: {}", e.getMessage(), e);
            return false;
        }
    }

    private static void convertShop(JsonObject shop, HolderLookup.Provider registries, VanillaItemCodec itemCodec, Path staging, Set<String> usedIds) throws IOException {
        String shopName = shop.get("shopName").getAsString();
        String id = CommonMethods.slugify(shopName, usedIds);
        usedIds.add(id);
        GUIShop.LOGGER.info("Converting shop '{}' -> id '{}'", shopName, id);

        ResourceId icon = readIcon(shop, ResourceId.ofVanilla("chest"));
        String defaultCurrency = shop.has("defaultCurrency") ? shop.get("defaultCurrency").getAsString() : null;

        int currentDataVersion = SharedConstants.getCurrentVersion().dataVersion().version();

        StringBuilder entries = new StringBuilder("[");
        JsonArray items = shop.has("items") ? shop.getAsJsonArray("items") : new JsonArray();
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String entrySnbt = convertItem(item, registries, itemCodec);
            if (entrySnbt == null) continue;
            if (entries.length() > 1) entries.append(",");
            entries.append(entrySnbt);
        }
        entries.append("]");

        StringBuilder shopSnbt = new StringBuilder("{");
        shopSnbt.append("DataVersion:").append(currentDataVersion).append(",");
        shopSnbt.append("displayName:").append(quote(shopName)).append(",");
        shopSnbt.append("icon:").append(quote(icon.toString()));
        if (defaultCurrency != null) {
            shopSnbt.append(",defaultCurrency:").append(quote(defaultCurrency));
        }
        shopSnbt.append(",entries:").append(entries);
        shopSnbt.append("}");

        Files.writeString(staging.resolve(id + ".snbt"), shopSnbt.toString(), StandardCharsets.UTF_8);
    }

    private static String convertItem(JsonObject item, HolderLookup.Provider registries, VanillaItemCodec itemCodec) {
        String itemId = item.get("itemId").getAsString();
        String name = item.get("name").getAsString();

        ItemStack stack = decodeLegacyStack(itemId, name, item.get("components"), registries, itemCodec);
        if (stack == null) return null;

        long buyPrice = item.get("buyPrice").getAsLong();
        long sellPrice = item.get("sellPrice").getAsLong();
        String currency = item.has("currency") ? item.get("currency").getAsString() : null;

        List<String> description = new LinkedList<>();
        if (item.has("description")) {
            item.getAsJsonArray("description").forEach(e -> description.add(e.getAsString()));
        }

        var stackNbt = itemCodec.encode(stack);

        StringBuilder entry = new StringBuilder("{");
        entry.append("displayName:").append(quote(name));
        if (!description.isEmpty()) {
            entry.append(",description:[");
            for (int i = 0; i < description.size(); i++) {
                if (i > 0) entry.append(",");
                entry.append(quote(description.get(i)));
            }
            entry.append("]");
        }
        entry.append(",buyPrice:").append(buyPrice).append("L");
        entry.append(",sellPrice:").append(sellPrice).append("L");
        if (currency != null) {
            entry.append(",currency:").append(quote(currency));
        }
        entry.append(",stack:").append(stackNbt.toString());
        entry.append("}");

        return entry.toString();
    }

    /// Legacy component JSON was written by whichever Minecraft version the server ran at the time,
    /// and several components changed shape since -- 1.21.5 unwrapped enchantments, stored_enchantments,
    /// dyed_color and attribute_modifiers, among others. Mojang's DataFixer already knows every one of
    /// those changes, so the listing is rebuilt as vanilla item NBT and run through the fixer instead of
    /// gui-shop maintaining its own list of shape rewrites.
    ///
    /// If the fixed stack still will not parse -- an unknown component from a mod that has since been
    /// removed, or a hand-edited value -- each component is retried on its own, so one unusable entry
    /// costs the item that component rather than its name, lore and enchantments as well.
    @Nullable
    private static ItemStack decodeLegacyStack(
        String itemId, String name, @Nullable JsonElement components,
        HolderLookup.Provider registries, VanillaItemCodec itemCodec
    ) {
        Optional<ItemStack> fixed = fixLegacyStack(itemId, components, registries, itemCodec.currentDataVersion());
        if (fixed.isPresent()) return fixed.get();

        Optional<Item> registryItem = lookupItem(itemId);
        if (registryItem.isEmpty()) {
            GUIShop.LOGGER.warn("Legacy item '{}' references unknown item id '{}', skipping", name, itemId);
            return null;
        }

        ItemStack stack = new ItemStack(registryItem.get(), 1);
        applySalvageableComponents(stack, components, registries.createSerializationContext(JsonOps.INSTANCE), itemId, name);
        return stack;
    }

    private static Optional<ItemStack> fixLegacyStack(
        String itemId, @Nullable JsonElement components, HolderLookup.Provider registries, int currentDataVersion
    ) {
        try {
            CompoundTag stackNbt = new CompoundTag();
            stackNbt.putString("id", itemId);
            stackNbt.putInt("count", 1);
            if (components != null && components.isJsonObject() && !components.getAsJsonObject().isEmpty()) {
                stackNbt.put("components", JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, components));
            }

            Dynamic<Tag> fixed = DataFixers.getDataFixer().update(
                References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, stackNbt),
                LEGACY_FLOOR_DATA_VERSION,
                currentDataVersion
            );

            return ItemStack.STRICT_SINGLE_ITEM_CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), fixed.getValue())
                .result();
        } catch (Exception e) {
            GUIShop.LOGGER.warn(
                "Could not data-fix legacy item '{}', falling back to per-component recovery: {}", itemId, e.getMessage()
            );
            return Optional.empty();
        }
    }

    private static void applySalvageableComponents(
        ItemStack stack, @Nullable JsonElement components, DynamicOps<JsonElement> ops, String itemId, String name
    ) {
        if (components == null || !components.isJsonObject()) return;

        components.getAsJsonObject().entrySet().forEach(entry -> {
            JsonObject single = new JsonObject();
            single.add(entry.getKey(), entry.getValue());

            DataComponentPatch.CODEC.parse(ops, single).resultOrPartial(err ->
                GUIShop.LOGGER.warn(
                    "Legacy item '{}' ({}): dropping component '{}' that could not be read: {}",
                    name, itemId, entry.getKey(), err
                )
            ).ifPresent(stack::applyComponentsAndValidate);
        });
    }

    private static Optional<Item> lookupItem(String itemId) {
        try {
            return ItemRegistry.getOptional(ResourceId.parse(itemId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /// A shop icon is decoration. A legacy value that is not a plain item id falls back to the
    /// default rather than failing the conversion, matching LegacyConfigMigrator#readIcon.
    private static ResourceId readIcon(JsonObject shop, ResourceId fallback) {
        if (!shop.has("icon")) return fallback;
        try {
            return ResourceId.parse(shop.get("icon").getAsString());
        } catch (Exception e) {
            GUIShop.LOGGER.warn("Legacy shop icon was not a plain item id, using default: {}", e.getMessage());
            return fallback;
        }
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static List<Path> listFiles(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.toList();
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {}
            });
        }
    }
}
