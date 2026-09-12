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

public class MinecraftItemCodec {
    private final RegistryOps<net.minecraft.nbt.Tag> ops;
    private final DataFixer fixer;
    private final int currentDataVersion;

    public MinecraftItemCodec(MinecraftServer server) {
        this(server.registryAccess());
    }

    public MinecraftItemCodec(HolderLookup.Provider registryLookup) {
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

    private static int readCurrentDataVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }
}
