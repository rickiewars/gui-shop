package rickiewars.guishop.serializer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.ResourceId;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemCodec;
import rickiewars.guishop.api.minecraft.impl.MinecraftItemStack;
import rickiewars.guishop.api.minecraft.impl.NbtCompat;
import rickiewars.guishop.shop.SellPricing;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SnbtShopStore {
    private static final String SNBT_EXTENSION = ".snbt";
    private static final String BACKUP_DIR_NAME = "backups";
    private static final java.time.format.DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final MinecraftItemCodec itemCodec;
    private final Path shopsDir;
    private final Path backupsDir;

    public SnbtShopStore(MinecraftItemCodec itemCodec, Path shopsDir) {
        this.itemCodec = itemCodec;
        this.shopsDir = shopsDir;
        this.backupsDir = shopsDir.resolve(BACKUP_DIR_NAME);
    }

    public List<Shop> readAll() {
        List<Shop> shops = new ArrayList<>();
        if (!Files.isDirectory(shopsDir)) return shops;

        try (Stream<Path> files = Files.list(shopsDir)) {
            for (Path file : files.filter(p -> p.toString().endsWith(SNBT_EXTENSION)).toList()) {
                readShop(file).ifPresent(shops::add);
            }
        } catch (IOException e) {
            GUIShop.LOGGER.error("Could not list shops directory {}: {}", shopsDir, e.getMessage());
        }

        return shops;
    }

    public Optional<Shop> readShop(Path file) {
        String id = fileNameWithoutExtension(file);
        CompoundTag envelope;
        try {
            envelope = NbtCompat.parseCompoundFully(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | CommandSyntaxException e) {
            GUIShop.LOGGER.error("Shop file {} failed to load: {}", file, e.getMessage());
            return Optional.empty();
        }

        Optional<Integer> storedVersion = NbtCompat.getInt(envelope, "DataVersion");
        if (storedVersion.isEmpty()) {
            GUIShop.LOGGER.error("Shop file {} failed to load: unknown or missing DataVersion", file);
            return Optional.empty();
        }

        if (storedVersion.get() > itemCodec.currentDataVersion()) {
            GUIShop.LOGGER.error(
                "Shop file {} failed to load: file saved for Minecraft version {} which was newer than this server",
                file, storedVersion.get()
            );
            return Optional.empty();
        }

        String displayName = NbtCompat.getString(envelope, "displayName").orElse(id);
        ResourceId icon = NbtCompat.getString(envelope, "icon").map(raw -> parseIdentifier(raw, "icon", "Shop file " + file)).orElse(null);
        ResourceId defaultCurrency = NbtCompat.getString(envelope, "defaultCurrency").map(raw -> parseIdentifier(raw, "defaultCurrency", "Shop file " + file)).orElse(null);
        SellPricing sellPricing = readSellPricing(envelope).orElse(null);

        List<ShopItem> items = new LinkedList<>();
        boolean[] needsRewrite = {false};
        ListTag entries = NbtCompat.getCompoundListOrEmpty(envelope, "entries");
        for (int i = 0; i < entries.size(); i++) {
            Optional<CompoundTag> entryCompound = NbtCompat.getCompound(entries, i);
            if (entryCompound.isEmpty()) continue;
            readEntry(entryCompound.get(), id, file, storedVersion.get(), needsRewrite).ifPresent(items::add);
        }

        Shop shop = new Shop(id, displayName, items, defaultCurrency, icon, sellPricing);

        if (needsRewrite[0]) {
            try {
                backupShop(file, id, storedVersion.get());
                writeShop(shop);
            } catch (IOException e) {
                GUIShop.LOGGER.error("Shop '{}': skipping rewrite to newer DataVersion since backup failed: {}", id, e.getMessage());
            }
        }

        return Optional.of(shop);
    }

    private Optional<ShopItem> readEntry(CompoundTag entry, String shopId, Path file, int storedVersion, boolean[] needsRewrite) {
        Optional<String> displayName = NbtCompat.getString(entry, "displayName");
        Optional<Long> buyPrice = NbtCompat.getLong(entry, "buyPrice");
        Optional<Long> sellPrice = NbtCompat.getLong(entry, "sellPrice");
        Optional<CompoundTag> stackNbt = NbtCompat.getCompound(entry, "stack");

        if (displayName.isEmpty() || buyPrice.isEmpty() || sellPrice.isEmpty() || stackNbt.isEmpty()) {
            GUIShop.LOGGER.warn("Shop '{}' in {}: entry missing a required field, skipping", shopId, file);
            return Optional.empty();
        }

        Optional<net.minecraft.world.item.ItemStack> decoded = itemCodec.decode(stackNbt.get(), storedVersion);
        if (decoded.isEmpty()) {
            GUIShop.LOGGER.warn("Shop '{}' in {}: entry '{}' could not be decoded, skipping", shopId, file, displayName.get());
            return Optional.empty();
        }
        if (storedVersion < itemCodec.currentDataVersion()) {
            needsRewrite[0] = true;
        }

        List<String> description = new LinkedList<>();
        NbtCompat.getStringList(entry, "description").ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                NbtCompat.getString(list, i).ifPresent(description::add);
            }
        });

        ResourceId explicitCurrency = NbtCompat.getString(entry, "currency")
            .map(raw -> parseIdentifier(raw, "currency", "Shop '" + shopId + "' in " + file + ", entry '" + displayName.get() + "'"))
            .orElse(null);

        return Optional.of(new ShopItem(
            displayName.get(),
            new MinecraftItemStack(decoded.get()),
            buyPrice.get(),
            sellPrice.get(),
            explicitCurrency,
            description
        ));
    }

    private void backupShop(Path file, String id, int oldDataVersion) throws IOException {
        String timestamp = BACKUP_TIMESTAMP_FORMAT.format(java.time.LocalDateTime.now());
        Path backupTarget = backupsDir.resolve(id + "-" + oldDataVersion + "-" + timestamp + SNBT_EXTENSION);

        Files.createDirectories(backupsDir);
        Files.copy(file, backupTarget);
    }

    public void writeShop(Shop shop) {
        try {
            Files.createDirectories(shopsDir);

            CompoundTag envelope = new CompoundTag();
            envelope.putInt("DataVersion", itemCodec.currentDataVersion());
            envelope.putString("displayName", shop.getDisplayName());
            envelope.putString("icon", shop.iconId().toString());
            if (shop.hasDefaultCurrency()) {
                envelope.putString("defaultCurrency", shop.getDefaultCurrencyId().toString());
            }

            ListTag entries = new ListTag();
            for (ShopItem item : shop.getItems()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("displayName", item.displayName());
                if (!item.description().isEmpty()) {
                    ListTag description = new ListTag();
                    item.description().forEach(line -> description.add(net.minecraft.nbt.StringTag.valueOf(line)));
                    entry.put("description", description);
                }
                entry.putLong("buyPrice", item.buyPrice());
                entry.putLong("sellPrice", item.sellPrice());
                ResourceId explicitCurrencyId = item.explicitCurrencyId();
                if (explicitCurrencyId != null) {
                    entry.putString("currency", explicitCurrencyId.toString());
                }
                entry.put("stack", itemCodec.encode(((MinecraftItemStack) item.stack()).stack()));
                entries.add(entry);
            }
            envelope.put("entries", entries);

            Path target = shopsDir.resolve(shop.getId() + SNBT_EXTENSION);
            Path tmp = shopsDir.resolve(shop.getId() + SNBT_EXTENSION + ".tmp");

            Files.writeString(tmp, envelope.toString(), StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            GUIShop.LOGGER.error("Could not write shop '{}' to file: {}", shop.getId(), e.getMessage());
        }
    }

    public void deleteShop(Shop shop) {
        try {
            Files.deleteIfExists(shopsDir.resolve(shop.getId() + SNBT_EXTENSION));
        } catch (IOException e) {
            GUIShop.LOGGER.error("Could not delete shop file for '{}': {}", shop.getId(), e.getMessage());
        }
    }

    private Optional<SellPricing> readSellPricing(CompoundTag envelope) {
        Optional<CompoundTag> compound = NbtCompat.getCompound(envelope, "sellPricing");
        if (compound.isEmpty()) return Optional.empty();
        CompoundTag sp = compound.get();

        SellPricing defaults = SellPricing.DEFAULT;
        return Optional.of(new SellPricing(
            NbtCompat.getDouble(sp, "firstUsePenalty").orElse(defaults.firstUsePenalty()),
            NbtCompat.getDouble(sp, "minValueFraction").orElse(defaults.minValueFraction()),
            NbtCompat.getDouble(sp, "damageCurveExponent").orElse(defaults.damageCurveExponent()),
            NbtCompat.getDouble(sp, "repairCostPenaltyPerPoint").orElse(defaults.repairCostPenaltyPerPoint()),
            NbtCompat.getDouble(sp, "customNamePenalty").orElse(defaults.customNamePenalty()),
            NbtCompat.getDouble(sp, "lorePenalty").orElse(defaults.lorePenalty())
        ));
    }

    private static ResourceId parseIdentifier(String raw, String field, String context) {
        ResourceId id = ResourceId.tryParse(raw);
        if (id == null) {
            GUIShop.LOGGER.warn("{}: invalid identifier '{}' for '{}', ignoring", context, raw, field);
        }
        return id;
    }

    private static String fileNameWithoutExtension(Path file) {
        String name = file.getFileName().toString();
        return name.endsWith(SNBT_EXTENSION) ? name.substring(0, name.length() - SNBT_EXTENSION.length()) : name;
    }
}
