package rickiewars.guishop.api.minecraft.impl;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import rickiewars.guishop.GUIShop;

import java.util.Optional;

/**
 * The only class in the mod that converts between ItemStack and NBT. Stores vanilla's own
 * ItemStack shape verbatim so Mojang's DataFixerUpper can migrate it forward across game updates,
 * instead of gui-shop maintaining its own hand-written item migration code.
 */
public class VanillaItemCodec {
    private final RegistryOps<net.minecraft.nbt.NbtElement> ops;
    private final DataFixer fixer;
    private final int currentDataVersion;

    public VanillaItemCodec(MinecraftServer server) {
        this(server.getRegistryManager());
    }

    public VanillaItemCodec(RegistryWrapper.WrapperLookup registryLookup) {
        this.ops = registryLookup.getOps(NbtOps.INSTANCE);
        this.fixer = Schemas.getFixer();
        this.currentDataVersion = readCurrentDataVersion();
    }

    public int currentDataVersion() {
        return currentDataVersion;
    }

    public NbtCompound encode(ItemStack stack) {
        return (NbtCompound) ItemStack.VALIDATED_UNCOUNTED_CODEC
            .encodeStart(ops, stack)
            .getOrThrow(msg -> new IllegalStateException("shop item encode failed: " + msg));
    }

    public Optional<ItemStack> decode(NbtCompound stackNbt, int storedDataVersion) {
        if (storedDataVersion > currentDataVersion) {
            GUIShop.LOGGER.error(
                "shop item data version {} is newer than the running server's {} -- refusing to load (downgrade?)",
                storedDataVersion, currentDataVersion
            );
            return Optional.empty();
        }

        NbtCompound working = stackNbt;
        if (storedDataVersion < currentDataVersion) {
            NbtCompound withCount = working.copy();
            if (!withCount.contains("count")) {
                withCount.putInt("count", 1);
            }
            Dynamic<net.minecraft.nbt.NbtElement> fixed = fixer.update(
                TypeReferences.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, withCount),
                storedDataVersion,
                currentDataVersion
            );
            working = (NbtCompound) fixed.getValue();
        }

        return ItemStack.VALIDATED_UNCOUNTED_CODEC
            .parse(ops, working)
            .resultOrPartial(err -> GUIShop.LOGGER.warn("shop item decode failed: {}", err));
    }

    /// Isolated per the migration spec: the WorldVersion/data-version accessor shape has moved
    /// before across Minecraft updates and may move again.
    private static int readCurrentDataVersion() {
        return SharedConstants.getGameVersion().dataVersion().id();
    }
}
