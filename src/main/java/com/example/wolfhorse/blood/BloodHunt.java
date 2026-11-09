package com.example.wolfhorse.blood;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One Blood Hunt: a howl, then three waves of DireWolves, with a red boss bar. */
public class BloodHunt {
    static final int WAVES = 3;
    private static final int[] WAVE_SIZE = {3, 4, 5};
    private static final int HOWL_TICKS = 100;          // the 5-second howl before the first wave
    private static final int BETWEEN_WAVES = 200;       // 10 seconds between waves
    private static final int TIME_LIMIT = 12000;        // 10 minutes, then the pack gives up
    private static final int NOBODY_AROUND_LIMIT = 3600; // 3 minutes with no players nearby ends it
    private static final double BAR_RANGE = 112.0D;

    enum State { HOWLING, WAVE, BETWEEN, WON, RETREAT, DONE }

    final UUID id = UUID.randomUUID();
    final ServerLevel level;
    final BlockPos center;
    final BloodHuntType type;
    final boolean fullMoon;
    @Nullable
    final UUID prey; // the player who blew the Blood Hunt Horn: the pack comes for them too
    private final ServerBossEvent bar;
    private final Set<UUID> hunters = new HashSet<>();
    private final long startTime;
    private State state = State.HOWLING;
    private int wave = 0;
    private int waveSize = 1;
    private long stateTime;
    private int nobodyAround = 0;

    BloodHunt(ServerLevel level, BlockPos center, BloodHuntType type, boolean fullMoon, @Nullable UUID prey) {
        this.level = level;
        this.center = center;
        this.type = type;
        this.fullMoon = fullMoon;
        this.prey = prey;
        this.startTime = level.getGameTime();
        this.stateTime = this.startTime;
        this.bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bar.setDarkenScreen(true);
        this.bar.setProgress(1.0F);
        this.setTitle(Component.translatable("bloodhunt.fotm.approaching", type.displayName()));
        HowlSounds.bloodHuntHowl(level, Vec3.atCenterOf(center));
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(Vec3.atCenterOf(center)) < 160 * 160) {
                p.displayClientMessage(Component.translatable("bloodhunt.fotm.howl").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    boolean isOver() {
        return this.state == State.DONE;
    }

    void hunterDied(UUID hunter) {
        this.hunters.remove(hunter);
    }

    /** Called every second. */
    void tick() {
        long now = this.level.getGameTime();
        this.updatePlayers();
        if (this.state == State.WON || this.state == State.RETREAT) {
            if (now - this.stateTime > 100) {
                this.end();
            }
            return;
        }
        boolean timeUp = now - this.startTime > TIME_LIMIT
                || (this.fullMoon && this.state != State.HOWLING && this.level.isDay());
        if (timeUp) {
            this.retreat();
            return;
        }
        if (this.bar.getPlayers().isEmpty()) {
            if ((this.nobodyAround += 20) > NOBODY_AROUND_LIMIT) {
                this.retreat();
                return;
            }
        } else {
            this.nobodyAround = 0;
        }
        switch (this.state) {
            case HOWLING, BETWEEN -> {
                int wait = this.state == State.HOWLING ? HOWL_TICKS : BETWEEN_WAVES;
                this.bar.setProgress(Mth.clamp((now - this.stateTime) / (float) wait, 0.0F, 1.0F)); // fills up
                if (now - this.stateTime >= wait) {
                    this.startWave();
                }
            }
            case WAVE -> {
                this.bar.setProgress(this.hunters.size() / (float) this.waveSize);
                if (this.hunters.isEmpty()) {
                    if (this.wave >= WAVES) {
                        this.win();
                    } else {
                        this.state = State.BETWEEN;
                        this.stateTime = now;
                        this.setTitle(Component.translatable("bloodhunt.fotm.between", this.type.displayName(), this.wave + 1, WAVES));
                    }
                }
            }
            default -> {
            }
        }
    }

    private void startWave() {
        this.wave++;
        this.state = State.WAVE;
        this.stateTime = this.level.getGameTime();
        this.waveSize = WAVE_SIZE[this.wave - 1];
        if (this.wave > 1) {
            HowlSounds.singleHowl(this.level, Vec3.atCenterOf(this.center));
        }
        for (int i = 0; i < this.waveSize; i++) {
            boolean frenzied = this.wave == WAVES && i == 0; // the Frenzied DireWolf leads the last wave
            WolfHorseEntity hunter = this.spawnHunter(frenzied);
            if (hunter != null) {
                this.hunters.add(hunter.getUUID());
            }
        }
        this.waveSize = Math.max(1, this.hunters.size());
        this.setTitle(Component.translatable("bloodhunt.fotm.wave", this.type.displayName(), this.wave, WAVES));
    }

    @Nullable
    private WolfHorseEntity spawnHunter(boolean frenzied) {
        WolfHorseEntity wolf = this.type.entityType().create(this.level);
        if (wolf == null) {
            return null;
        }
        BlockPos spot = this.findSpawnSpot();
        wolf.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, this.level.random.nextFloat() * 360.0F, 0.0F);
        wolf.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(spot), MobSpawnType.EVENT,
                new AgeableMob.AgeableMobGroupData(false), null);
        wolf.makeBloodHunter(this.id, this.type, this.prey);
        if (frenzied) {
            wolf.setCustomName(Component.translatable("entity.fotm.frenzied_direwolf").withStyle(ChatFormatting.DARK_RED));
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D);
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(9.0D);
            wolf.setHealth(wolf.getMaxHealth());
        }
        this.level.addFreshEntity(wolf);
        this.level.sendParticles(ParticleTypes.LARGE_SMOKE, wolf.getX(), wolf.getY() + 0.8D, wolf.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.02D);
        return wolf;
    }

    /** A spot on the ground 26-34 blocks from the center, in a loaded chunk. */
    private BlockPos findSpawnSpot() {
        for (int tries = 0; tries < 16; tries++) {
            double angle = this.level.random.nextDouble() * Math.PI * 2.0D;
            double dist = 26.0D + this.level.random.nextDouble() * 8.0D;
            int x = this.center.getX() + Mth.floor(Math.cos(angle) * dist);
            int z = this.center.getZ() + Mth.floor(Math.sin(angle) * dist);
            if (!this.level.hasChunkAt(new BlockPos(x, this.center.getY(), z))) {
                continue;
            }
            int y = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (this.level.getFluidState(pos.below()).isEmpty()) {
                return pos;
            }
        }
        return this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.center);
    }

    private void win() {
        this.state = State.WON;
        this.stateTime = this.level.getGameTime();
        this.bar.setProgress(0.0F);
        this.setTitle(Component.translatable("bloodhunt.fotm.victory"));
        for (ServerPlayer p : this.bar.getPlayers()) {
            p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        // the pack's spoils, dropped at the middle of the hunt
        BlockPos drop = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.center);
        LootTable table = this.level.getServer().getLootData().getLootTable(this.type.rewardLootTable());
        LootParams params = new LootParams.Builder(this.level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(drop))
                .create(LootContextParamSets.CHEST);
        for (ItemStack stack : table.getRandomItems(params)) {
            ItemEntity item = new ItemEntity(this.level, drop.getX() + 0.5D, drop.getY() + 0.5D, drop.getZ() + 0.5D, stack);
            item.setDefaultPickUpDelay();
            this.level.addFreshEntity(item);
        }
        this.level.sendParticles(ParticleTypes.HAPPY_VILLAGER, drop.getX() + 0.5D, drop.getY() + 1.0D, drop.getZ() + 0.5D, 20, 1.0D, 0.6D, 1.0D, 0.0D);
    }

    /** Time ran out (sunrise, 10 minutes, or everyone left): the remaining pack disappears. */
    void retreat() {
        if (this.state == State.DONE) {
            return;
        }
        this.state = State.RETREAT;
        this.stateTime = this.level.getGameTime();
        this.setTitle(Component.translatable("bloodhunt.fotm.retreat"));
        for (UUID u : new HashSet<>(this.hunters)) {
            Entity e = this.level.getEntity(u);
            if (e != null) {
                this.level.sendParticles(ParticleTypes.POOF, e.getX(), e.getY() + 0.8D, e.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.02D);
                e.discard();
            }
        }
        this.hunters.clear();
    }

    void end() {
        this.state = State.DONE;
        this.bar.removeAllPlayers();
    }

    private void setTitle(Component text) {
        this.bar.setName(text.copy().withStyle(ChatFormatting.DARK_RED));
    }

    private void updatePlayers() {
        Vec3 c = Vec3.atCenterOf(this.center);
        for (ServerPlayer p : this.level.players()) {
            boolean near = p.isAlive() && !p.isSpectator() && p.distanceToSqr(c) < BAR_RANGE * BAR_RANGE;
            if (near && !this.bar.getPlayers().contains(p)) {
                this.bar.addPlayer(p);
            } else if (!near && this.bar.getPlayers().contains(p)) {
                this.bar.removePlayer(p);
            }
        }
    }
}
