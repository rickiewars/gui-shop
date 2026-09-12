package rickiewars.guishop.api.minecraft.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;

import java.util.Optional;

/// Shims {@link CompoundTag}/{@link ListTag} reads to the Optional-returning shape they gained in
/// 1.21.5. Below that version the same-named methods return raw defaulted primitives instead, so
/// callers guard with `contains` first.
public final class NbtCompat {
    private NbtCompat() {}

    public static CompoundTag parseCompoundFully(String raw) throws CommandSyntaxException {
        //? if >=1.21.5 {
        return TagParser.parseCompoundFully(raw);
        //?} else {
        /*return TagParser.parseTag(raw);
        *///?}
    }

    public static Optional<String> getString(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getString(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getString(key)) : Optional.empty();
        *///?}
    }

    public static Optional<Integer> getInt(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getInt(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getInt(key)) : Optional.empty();
        *///?}
    }

    public static Optional<Long> getLong(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getLong(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getLong(key)) : Optional.empty();
        *///?}
    }

    public static Optional<Double> getDouble(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getDouble(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getDouble(key)) : Optional.empty();
        *///?}
    }

    public static Optional<CompoundTag> getCompound(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getCompound(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getCompound(key)) : Optional.empty();
        *///?}
    }

    public static ListTag getCompoundListOrEmpty(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getListOrEmpty(key);
        //?} else {
        /*return tag.contains(key) ? tag.getList(key, 10) : new ListTag();
        *///?}
    }

    public static Optional<ListTag> getStringList(CompoundTag tag, String key) {
        //? if >=1.21.5 {
        return tag.getList(key);
        //?} else {
        /*return tag.contains(key) ? Optional.of(tag.getList(key, 8)) : Optional.empty();
        *///?}
    }

    public static Optional<CompoundTag> getCompound(ListTag list, int index) {
        //? if >=1.21.5 {
        return list.getCompound(index);
        //?} else {
        /*return index >= 0 && index < list.size() ? Optional.of(list.getCompound(index)) : Optional.empty();
        *///?}
    }

    public static Optional<String> getString(ListTag list, int index) {
        //? if >=1.21.5 {
        return list.getString(index);
        //?} else {
        /*return index >= 0 && index < list.size() ? Optional.of(list.getString(index)) : Optional.empty();
        *///?}
    }
}
