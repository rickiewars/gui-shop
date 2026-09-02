package rickiewars.guishop.serializer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.impl.VanillaItemCodec;
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

    private final VanillaItemCodec itemCodec;
    private final Path shopsDir;
    private final Path backupsDir;

    public SnbtShopStore(VanillaItemCodec itemCodec, Path shopsDir) {
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
        NbtCompound envelope;
        try {
            envelope = StringNbtReader.readCompound(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | CommandSyntaxException e) {
            GUIShop.LOGGER.error("Shop file {} failed to load: {}", file, e.getMessage());
            return Optional.empty();
        }

        Optional<Integer> storedVersion = envelope.getInt("DataVersion");
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

        String displayName = envelope.getString("displayName").orElse(id);
        Identifier icon = envelope.getString("icon").map(raw -> parseIdentifier(raw, "icon", "Shop file " + file)).orElse(null);
        Identifier defaultCurrency = envelope.getString("defaultCurrency").map(raw -> parseIdentifier(raw, "defaultCurrency", "Shop file " + file)).orElse(null);
        SellPricing sellPricing = readSellPricing(envelope).orElse(null);

        List<ShopItem> items = new LinkedList<>();
        boolean[] needsRewrite = {false};
        NbtList entries = envelope.getListOrEmpty("entries");
        for (int i = 0; i < entries.size(); i++) {
            Optional<NbtCompound> entryCompound = entries.getCompound(i);
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

    private Optional<ShopItem> readEntry(NbtCompound entry, String shopId, Path file, int storedVersion, boolean[] needsRewrite) {
        Optional<String> displayName = entry.getString("displayName");
        Optional<Long> buyPrice = entry.getLong("buyPrice");
        Optional<Long> sellPrice = entry.getLong("sellPrice");
        Optional<NbtCompound> stackNbt = entry.getCompound("stack");

        if (displayName.isEmpty() || buyPrice.isEmpty() || sellPrice.isEmpty() || stackNbt.isEmpty()) {
            GUIShop.LOGGER.warn("Shop '{}' in {}: entry missing a required field, skipping", shopId, file);
            return Optional.empty();
        }

        Optional<net.minecraft.item.ItemStack> decoded = itemCodec.decode(stackNbt.get(), storedVersion);
        if (decoded.isEmpty()) {
            GUIShop.LOGGER.warn("Shop '{}' in {}: entry '{}' could not be decoded, skipping", shopId, file, displayName.get());
            return Optional.empty();
        }
        if (storedVersion < itemCodec.currentDataVersion()) {
            needsRewrite[0] = true;
        }

        List<String> description = new LinkedList<>();
        entry.getList("description").ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                list.getString(i).ifPresent(description::add);
            }
        });

        Identifier explicitCurrency = entry.getString("currency")
            .map(raw -> parseIdentifier(raw, "currency", "Shop '" + shopId + "' in " + file + ", entry '" + displayName.get() + "'"))
            .orElse(null);

        return Optional.of(new ShopItem(
            displayName.get(),
            decoded.get(),
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

            NbtCompound envelope = new NbtCompound();
            envelope.putInt("DataVersion", itemCodec.currentDataVersion());
            envelope.putString("displayName", shop.getDisplayName());
            envelope.putString("icon", shop.iconId().toString());
            if (shop.hasDefaultCurrency()) {
                envelope.putString("defaultCurrency", shop.getDefaultCurrencyId().toString());
            }

            NbtList entries = new NbtList();
            for (ShopItem item : shop.getItems()) {
                NbtCompound entry = new NbtCompound();
                entry.putString("displayName", item.displayName());
                if (!item.description().isEmpty()) {
                    NbtList description = new NbtList();
                    item.description().forEach(line -> description.add(net.minecraft.nbt.NbtString.of(line)));
                    entry.put("description", description);
                }
                entry.putLong("buyPrice", item.buyPrice());
                entry.putLong("sellPrice", item.sellPrice());
                Identifier explicitCurrencyId = item.explicitCurrencyId();
                if (explicitCurrencyId != null) {
                    entry.putString("currency", explicitCurrencyId.toString());
                }
                entry.put("stack", itemCodec.encode(item.stack()));
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

    private Optional<SellPricing> readSellPricing(NbtCompound envelope) {
        Optional<NbtCompound> compound = envelope.getCompound("sellPricing");
        if (compound.isEmpty()) return Optional.empty();
        NbtCompound sp = compound.get();

        SellPricing defaults = SellPricing.DEFAULT;
        return Optional.of(new SellPricing(
            sp.getDouble("firstUsePenalty").orElse(defaults.firstUsePenalty()),
            sp.getDouble("minValueFraction").orElse(defaults.minValueFraction()),
            sp.getDouble("damageCurveExponent").orElse(defaults.damageCurveExponent()),
            sp.getDouble("repairCostPenaltyPerPoint").orElse(defaults.repairCostPenaltyPerPoint()),
            sp.getDouble("customNamePenalty").orElse(defaults.customNamePenalty()),
            sp.getDouble("lorePenalty").orElse(defaults.lorePenalty())
        ));
    }

    private static Identifier parseIdentifier(String raw, String field, String context) {
        Identifier id = Identifier.tryParse(raw);
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
