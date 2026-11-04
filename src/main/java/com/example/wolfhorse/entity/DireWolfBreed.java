package com.example.wolfhorse.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;

/** Which coats each DireWolf type can be born with. */
public enum DireWolfBreed {
    // brown to black
    FOREST(new Variant[]{Variant.BROWN, Variant.DARK_BROWN, Variant.BLACK},
            new Markings[]{Markings.NONE, Markings.NONE, Markings.BLACK_DOTS}),
    // tan to dark grey
    PLAINS(new Variant[]{Variant.CREAMY, Variant.CREAMY, Variant.GRAY},
            new Markings[]{Markings.NONE, Markings.NONE, Markings.BLACK_DOTS}),
    // white to grey
    TUNDRA(new Variant[]{Variant.WHITE, Variant.WHITE, Variant.GRAY},
            new Markings[]{Markings.NONE, Markings.WHITE, Markings.WHITE_FIELD});

    private final Variant[] coats;
    private final Markings[] markings;

    DireWolfBreed(Variant[] coats, Markings[] markings) {
        this.coats = coats;
        this.markings = markings;
    }

    public Variant randomCoat(RandomSource random) {
        return coats[random.nextInt(coats.length)];
    }

    public Markings randomMarkings(RandomSource random) {
        return markings[random.nextInt(markings.length)];
    }
}
