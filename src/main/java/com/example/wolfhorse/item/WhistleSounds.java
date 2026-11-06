package com.example.wolfhorse.item;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Plays a held whistle note: the same flute note repeated quickly and fading out,
 * so it sounds like one longer whistle instead of a short blip.
 */
@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID)
public final class WhistleSounds {
    private static final int NOTES = 5;           // one held whistle = 5 overlapping notes
    private static final int TICKS_BETWEEN = 2;   // ~0.5 seconds in total

    private record Note(ServerLevel level, Player player, float pitch, float volume, long playAt) {
    }

    private static final List<Note> PENDING = new ArrayList<>();

    private WhistleSounds() {
    }

    public static void play(ServerLevel level, Player player, float pitch) {
        long now = level.getGameTime();
        for (int i = 0; i < NOTES; i++) {
            float volume = 1.0F - i * 0.15F;
            Note note = new Note(level, player, pitch, volume, now + (long) i * TICKS_BETWEEN);
            if (i == 0) {
                sound(note);
            } else {
                PENDING.add(note);
            }
        }
    }

    private static void sound(Note n) {
        n.level().playSound(null, n.player().getX(), n.player().getY(), n.player().getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, n.volume(), n.pitch());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        Iterator<Note> it = PENDING.iterator();
        while (it.hasNext()) {
            Note n = it.next();
            if (n.player().isRemoved()) {
                it.remove();
            } else if (n.level().getGameTime() >= n.playAt()) {
                sound(n);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
