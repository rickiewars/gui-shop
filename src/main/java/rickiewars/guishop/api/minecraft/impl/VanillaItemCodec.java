package rickiewars.guishop.api.minecraft.impl;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import rickiewars.guishop.GUIShop;

import java.util.Optional;

/**
 * The only class in the mod that converts between ItemStack and NBT. Stores vanilla's own
 * ItemStack shape verbatim so Mojang's DataFixerUpper can migrate it forward across game updates,
 * instead of gui-shop maintaining its own hand-written item migration code.
 */
public class VanillaItemCodec {
    private final RegistryOps<net.minecraft.nbt.Tag> ops;
    private final DataFixer fixer;
    private final int currentDataVersion;

    public VanillaItemCodec(MinecraftServer server) {
        this(server.registryAccess());
    }

    public VanillaItemCodec(HolderLookup.Provider registryLookup) {
        this.ops = registryLookup.createSerializationContext(NbtOps.INSTANCE);
        this.fixer = DataFixers.getDataFixer();
        this.currentDataVersion = readCurrentDataVersion();
    }

    public int currentDataVersion() {
        return currentDataVersion;
    }

    public CompoundTag encode(ItemStack stack) {
        return (CompoundTag) ItemStack.STRICT_SINGLE_ITEM_CODEC
            .encodeStart(ops, stack)
            .getOrThrow(msg -> new IllegalStateException("shop item encode failed: " + msg));
    }

    public Optional<ItemStack> decode(CompoundTag stackNbt, int storedDataVersion) {
        if (storedDataVersion > currentDataVersion) {
            GUIShop.LOGGER.error(
                "shop item data version {} is newer than the running server's {} -- refusing to load (downgrade?)",
                storedDataVersion, currentDataVersion
            );
            return Optional.empty();
        }

        CompoundTag working = stackNbt;
        if (storedDataVersion < currentDataVersion) {
            CompoundTag withCount = working.copy();
            if (!withCount.contains("count")) {
                withCount.putInt("count", 1);
            }
            Dynamic<net.minecraft.nbt.Tag> fixed = fixer.update(
                References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, withCount),
                storedDataVersion,
                currentDataVersion
            );
            working = (CompoundTag) fixed.getValue();
        }

        return ItemStack.STRICT_SINGLE_ITEM_CODEC
            .parse(ops, working)
            .resultOrPartial(err -> GUIShop.LOGGER.warn("shop item decode failed: {}", err));
    }

    /// Isolated per the migration spec: the WorldVersion/data-version accessor shape has moved
    /// before across Minecraft updates and may move again.
    private static int readCurrentDataVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }
}
