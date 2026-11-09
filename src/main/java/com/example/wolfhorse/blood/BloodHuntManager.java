package com.example.wolfhorse.blood;

import com.example.wolfhorse.WolfHorseConfig;
import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.WolfHorseEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs Blood Hunts: starts them on full moons (and from the Blood Hunt Horn or /bloodhunt),
 * ticks them, and handles the hunters dying.
 */
@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID)
public final class BloodHuntManager {
    public static final TagKey<EntityType<?>> LIVESTOCK =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(WolfHorseMod.MODID, "livestock"));
    private static final double MIN_DISTANCE_BETWEEN_HUNTS = 128.0D;

    private static final Map<UUID, BloodHunt> HUNTS = new LinkedHashMap<>();
    private static long lastFullMoonNight = -1;

    private BloodHuntManager() {
    }

    // ---------- queries used by the DireWolves ----------

    public static boolean isActive(UUID huntId) {
        BloodHunt h = HUNTS.get(huntId);
        return h != null && !h.isOver();
    }

    @Nullable
    public static BlockPos center(UUID huntId) {
        BloodHunt h = HUNTS.get(huntId);
        return h == null ? null : h.center;
    }

    public static boolean huntNear(Level level, BlockPos pos) {
        for (BloodHunt h : HUNTS.values()) {
            if (!h.isOver() && h.level == level
                    && h.center.distSqr(pos) < MIN_DISTANCE_BETWEEN_HUNTS * MIN_DISTANCE_BETWEEN_HUNTS) {
                return true;
            }
        }
        return false;
    }

    // ---------- starting a hunt ----------

    /** Starts a Blood Hunt here. Returns false if one is already going on nearby. */
    public static boolean start(ServerLevel level, BlockPos center, BloodHuntType type, boolean fullMoon,
                                @Nullable UUID prey) {
        if (huntNear(level, center)) {
            return false;
        }
        BloodHunt hunt = new BloodHunt(level, center, type, fullMoon, prey);
        HUNTS.put(hunt.id, hunt);
        return true;
    }

    /** Somewhere worth hunting near a player: a village, or a few farm animals. */
    private static Optional<BlockPos> findPrey(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        Optional<BlockPos> village = level.getPoiManager()
                .findClosest(p -> p.is(PoiTypeTags.VILLAGE), pos, 64, PoiManager.Occupancy.ANY);
        if (village.isPresent()) {
            return village;
        }
        int animals = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(48.0D),
                e -> e.getType().is(LIVESTOCK)).size();
        return animals >= 3 ? Optional.of(pos) : Optional.empty();
    }

    // ---------- ticking ----------

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ServerLevel overworld = event.getServer().overworld();
        if (overworld.getGameTime() % 20 != 0) {
            return;
        }
        for (BloodHunt hunt : new ArrayList<>(HUNTS.values())) {
            hunt.tick();
            if (hunt.isOver()) {
                HUNTS.remove(hunt.id);
            }
        }
        checkFullMoon(overworld);
    }

    /** As a full-moon night begins, each player near a village or livestock may get a Blood Hunt. */
    private static void checkFullMoon(ServerLevel level) {
        if (!WolfHorseConfig.fullMoonBloodHunts()) {
            return;
        }
        long dayTime = level.getDayTime();
        long night = dayTime / 24000L;
        long timeOfDay = dayTime % 24000L;
        if (level.getMoonPhase() != 0 || timeOfDay < 13000L || timeOfDay > 14000L || night == lastFullMoonNight) {
            return;
        }
        lastFullMoonNight = night;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || level.random.nextDouble() >= WolfHorseConfig.fullMoonBloodHuntChance()) {
                continue;
            }
            findPrey(level, player).ifPresent(site ->
                    start(level, site, BloodHuntType.forBiome(level, site), true, null));
        }
    }

    // ---------- hunters dying ----------

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide) {
            return;
        }
        if (dead instanceof WolfHorseEntity wolf && wolf.isBloodHunter()) {
            BloodHunt hunt = HUNTS.get(wolf.getBloodHuntId());
            if (hunt != null) {
                hunt.hunterDied(wolf.getUUID());
            }
        }
        // Dark Forest packs eat the villagers they kill
        if (dead instanceof AbstractVillager && event.getSource().getEntity() instanceof WolfHorseEntity hunter
                && hunter.isBloodHunter() && hunter.getBloodHuntType() == BloodHuntType.DARK_FOREST
                && dead.level() instanceof ServerLevel level) {
            hunter.heal(10.0F);
            level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0F, 0.7F);
            level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 1.0F, 0.8F);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.55F, 0.0F, 0.0F), 1.6F),
                    dead.getX(), dead.getY() + 0.6D, dead.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.0D);
        }
    }

    /** Blood Hunt hunters drop a Bloody Bone more often than ordinary DireWolves. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof WolfHorseEntity wolf && wolf.isBloodHunter()
                && event.getSource().getEntity() instanceof ServerPlayer
                && wolf.getRandom().nextFloat() < 0.15F + 0.05F * event.getLootingLevel()) {
            event.getDrops().add(new ItemEntity(wolf.level(), wolf.getX(), wolf.getY(), wolf.getZ(),
                    new ItemStack(WolfHorseMod.BLOODY_BONE.get())));
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HUNTS.values().forEach(BloodHunt::end);
        HUNTS.clear();
        lastFullMoonNight = -1;
    }

    // ---------- /bloodhunt command (operators) ----------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bloodhunt")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("start")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(BloodHuntType.values()).map(t -> t.id), b))
                                .executes(ctx -> startFromCommand(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "type"), BlockPos.containing(ctx.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> startFromCommand(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "type"), BlockPosArgument.getBlockPos(ctx, "pos"))))))
                .then(Commands.literal("stop").executes(ctx -> {
                    int n = 0;
                    for (BloodHunt h : HUNTS.values()) {
                        if (!h.isOver()) {
                            h.retreat();
                            n++;
                        }
                    }
                    int stopped = n;
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.fotm.bloodhunt.stopped", stopped), true);
                    return stopped;
                })));
    }

    private static int startFromCommand(CommandSourceStack src, String typeId, BlockPos pos) {
        BloodHuntType type = BloodHuntType.byId(typeId);
        if (type == null) {
            src.sendFailure(Component.translatable("commands.fotm.bloodhunt.unknown", typeId));
            return 0;
        }
        if (!start(src.getLevel(), pos, type, false, null)) {
            src.sendFailure(Component.translatable("message.fotm.bloodhunt.already"));
            return 0;
        }
        src.sendSuccess(() -> Component.translatable("commands.fotm.bloodhunt.started", type.displayName(),
                pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    /** Used by the hunters so they keep to the hunt area. */
    public static boolean nearCenter(UUID huntId, Vec3 pos, double range) {
        BlockPos c = center(huntId);
        return c != null && c.distToCenterSqr(pos) < range * range;
    }
}
