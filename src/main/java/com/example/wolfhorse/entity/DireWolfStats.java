package com.example.wolfhorse.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * DireWolf stat ranges and the random rolls for them.
 *
 *   Speed:  5.5 to 16 blocks/sec when ridden, average 10 (Plains: 11.5)
 *   Health: 18 to 35,                         average 26.5 (Tundra: 29)
 *   Jump:   2 to 6 blocks high,               average 4 (Forest: 4.75)
 *
 * A roll is bell-shaped (most land near the average, extremes are rare), like vanilla horses.
 */
public final class DireWolfStats {
    public static final double SPEED_MIN = 5.5D, SPEED_MAX = 16.0D;
    public static final double HEALTH_MIN = 18.0D, HEALTH_MAX = 35.0D;
    public static final double JUMP_MIN = 2.0D, JUMP_MAX = 6.0D;

    /** Riding speed in blocks per second for each point of the movement-speed attribute. */
    private static final double BLOCKS_PER_SEC = 43.0D;
    /** Average distance of a 3-dice roll from its middle, times two (exactly 13/48). */
    private static final double SPREAD = 13.0D / 48.0D;

    private DireWolfStats() {
    }

    public static double speedAverage(DireWolfBreed breed) {
        return breed == DireWolfBreed.PLAINS ? 11.5D : 10.0D;
    }

    public static double healthAverage(DireWolfBreed breed) {
        return breed == DireWolfBreed.TUNDRA ? 29.0D : 26.5D;
    }

    public static double jumpAverage(DireWolfBreed breed) {
        return breed == DireWolfBreed.FOREST ? 4.75D : 4.0D;
    }

    /** A bell-shaped random value from min to max whose long-run average is the given average. */
    public static double roll(RandomSource random, double min, double max, double average) {
        double f = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0D;
        // the value a "middle" roll gets, chosen so the overall average comes out right
        double middle = Mth.clamp((average - SPREAD * (min + max) / 2.0D) / (1.0D - SPREAD), min, max);
        return f < 0.5D
                ? middle - (middle - min) * (0.5D - f) * 2.0D
                : middle + (max - middle) * (f - 0.5D) * 2.0D;
    }

    public static double rollSpeedBlocks(RandomSource r, DireWolfBreed b) {
        return roll(r, SPEED_MIN, SPEED_MAX, speedAverage(b));
    }

    public static double rollHealth(RandomSource r, DireWolfBreed b) {
        return Math.round(roll(r, HEALTH_MIN, HEALTH_MAX, healthAverage(b)));
    }

    public static double rollJumpBlocks(RandomSource r, DireWolfBreed b) {
        return roll(r, JUMP_MIN, JUMP_MAX, jumpAverage(b));
    }

    // ---------- conversions to the game's attribute values ----------

    public static double speedAttribute(double blocksPerSecond) {
        return blocksPerSecond / BLOCKS_PER_SEC;
    }

    public static double speedBlocks(double attribute) {
        return attribute * BLOCKS_PER_SEC;
    }

    /** How high a horse-style jump goes for a jump-strength value (the standard horse formula). */
    public static double jumpBlocks(double strength) {
        return -0.1817584952D * strength * strength * strength
                + 3.689713992D * strength * strength
                + 2.128599134D * strength
                - 0.343930367D;
    }

    /** The jump strength that reaches the given height. */
    public static double jumpStrength(double blocks) {
        double lo = 0.1D, hi = 2.0D;
        for (int i = 0; i < 40; i++) {
            double mid = (lo + hi) / 2.0D;
            if (jumpBlocks(mid) < blocks) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) / 2.0D;
    }
}
