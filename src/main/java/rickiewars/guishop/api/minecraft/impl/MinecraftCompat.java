package rickiewars.guishop.api.minecraft.impl;

import com.mojang.authlib.GameProfile;

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
}
