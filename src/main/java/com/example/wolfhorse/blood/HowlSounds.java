package com.example.wolfhorse.blood;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wolf howls built from the vanilla wolf howl, layered and pitched down so they last longer.
 *  - bloodHuntHowl: about 5 seconds, a pack howling one after another, heard far away
 *  - singleHowl:    one long howl (the Howling Horn)
 */
@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID)
public final class HowlSounds {
    private record Note(ServerLevel level, Vec3 pos, float volume, float pitch, long playAt) {
    }

    private static final List<Note> PENDING = new ArrayList<>();

    private HowlSounds() {
    }

    /** ~5 seconds: four overlapping howls, low and drawn-out. Volume 8 carries about 128 blocks. */
    public static void bloodHuntHowl(ServerLevel level, Vec3 pos) {
        schedule(level, pos, 8.0F, 0.70F, 0);
        schedule(level, pos, 7.0F, 0.85F, 22);
        schedule(level, pos, 7.0F, 0.62F, 45);
        schedule(level, pos, 6.0F, 0.78F, 62);
    }

    /** One long howl, ~3 seconds, heard about 48 blocks away. */
    public static void singleHowl(ServerLevel level, Vec3 pos) {
        schedule(level, pos, 3.0F, 0.80F, 0);
        schedule(level, pos, 2.0F, 0.95F, 18);
    }

    private static void schedule(ServerLevel level, Vec3 pos, float volume, float pitch, int delayTicks) {
        Note n = new Note(level, pos, volume, pitch, level.getGameTime() + delayTicks);
        if (delayTicks == 0) {
            play(n);
        } else {
            PENDING.add(n);
        }
    }

    private static void play(Note n) {
        n.level().playSound(null, n.pos().x, n.pos().y, n.pos().z, SoundEvents.WOLF_HOWL, SoundSource.HOSTILE,
                n.volume(), n.pitch());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        Iterator<Note> it = PENDING.iterator();
        while (it.hasNext()) {
            Note n = it.next();
            if (n.level().getGameTime() >= n.playAt()) {
                play(n);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
