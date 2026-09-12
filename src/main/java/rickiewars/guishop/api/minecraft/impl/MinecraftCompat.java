package rickiewars.guishop.api.minecraft.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.ResourceKey;

import java.net.URI;
import java.util.UUID;

public final class MinecraftCompat {
    private MinecraftCompat() {}

    public static UUID id(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.id();
        //?} else {
        /*return profile.getId();
        *///?}
    }

    public static int currentDataVersion() {
        //? if >=1.21.6 {
        return SharedConstants.getCurrentVersion().dataVersion().version();
        //?} else {
        /*return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        *///?}
    }

    public static ClickEvent clickEventOpenUrl(String url) {
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

    public static ClickEvent clickEventCopyToClipboard(String text) {
        //? if >=1.21.5 {
        return new ClickEvent.CopyToClipboard(text);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
        *///?}
    }

    public static <T> Holder<T> getOrThrow(HolderLookup.Provider registries, ResourceKey<T> key) {
        //? if >=1.21.5 {
        return registries.getOrThrow(key);
        //?} else {
        /*return registries.lookupOrThrow(key.registryKey()).getOrThrow(key);
        *///?}
    }
}
