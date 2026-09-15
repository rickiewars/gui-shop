package rickiewars.guishop.api.minecraft.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

import java.net.URI;
import java.util.UUID;

public interface MinecraftCompat {

    static UUID id(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.id();
        //?} else {
        /*return profile.getId();
        *///?}
    }

    static int currentDataVersion() {
        //? if >=1.21.6 {
        return SharedConstants.getCurrentVersion().dataVersion().version();
        //?} else {
        /*return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        *///?}
    }

    static ClickEvent clickEventOpenUrl(String url) {
        //? if >=1.21.5 {
        try {
            return new ClickEvent.OpenUrl(new URI(url));
        } catch (Exception e) {
            return clickEventCopyToClipboard(url);
        }
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        *///?}
    }

    static ClickEvent clickEventCopyToClipboard(String text) {
        //? if >=1.21.5 {
        return new ClickEvent.CopyToClipboard(text);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
        *///?}
    }

    static ItemStack createItemStack(ItemInput itemInput, int count) throws CommandSyntaxException {
        //? if >=26.1 {
        return itemInput.createItemStack(count);
        //?} else {
        /*return itemInput.createItemStack(count, false);
        *///?}
    }

    static <T> Holder<T> getOrThrow(HolderLookup.Provider registries, ResourceKey<T> key) {
        //? if >=1.21.5 {
        return registries.getOrThrow(key);
        //?} else {
        /*return registries.lookupOrThrow(key.registryKey()).getOrThrow(key);
        *///?}
    }

    static HolderLookup.Provider vanillaRegistries() {
        //? if >=26.3 {
        return net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(
            net.minecraft.data.registries.VanillaRegistries.createWorldLookup()
        );
        //?} else {
        /*return net.minecraft.data.registries.VanillaRegistries.createLookup();
        *///?}
    }
}
