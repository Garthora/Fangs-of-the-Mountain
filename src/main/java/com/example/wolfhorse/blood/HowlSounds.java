package com.example.wolfhorse.blood;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * DireWolf howls. Each call is one wolf howling once. The sound has eight variations
 * (see assets/fotm/sounds.json), and each howl gets a little extra random pitch on top.
 */
public final class HowlSounds {
    /** A Blood Hunt wolf arriving: carries about 75 blocks. */
    public static final float HUNT_VOLUME = 4.8F;
    /** The Howling Horn: carries about 30 blocks. */
    public static final float HORN_VOLUME = 1.8F;

    private HowlSounds() {
    }

    public static void howl(ServerLevel level, Vec3 pos, float volume) {
        float pitch = 0.92F + level.random.nextFloat() * 0.16F;
        level.playSound(null, pos.x, pos.y, pos.z, WolfHorseMod.DIREWOLF_HOWL.get(), SoundSource.HOSTILE, volume, pitch);
    }
}
