package com.example.wolfhorse.entity;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.goal.WolfHorseFollowOwnerGoal;
import com.example.wolfhorse.entity.goal.WolfHorseMeleeAttackGoal;
import com.example.wolfhorse.entity.goal.WolfHorseOwnerHurtByTargetGoal;
import com.example.wolfhorse.entity.goal.WolfHorseOwnerHurtTargetGoal;
import com.example.wolfhorse.entity.goal.WolfHorseSitGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A rideable horse with a wolf's brain:
 *  - tamed with bones (1 in 3 chance per bone), not by breaking it in
 *  - sits on command, follows its owner, teleports to catch up
 *  - defends its owner and attacks whatever its owner attacks
 *  - wild ones hunt sheep and rabbits and attack skeletons
 *  - eats meat to heal and breed
 *  - can still be saddled, armored and ridden like a normal horse
 */
public class WolfHorseEntity extends Horse {
    private static final EntityDataAccessor<Boolean> DATA_SITTING =
            SynchedEntityData.defineId(WolfHorseEntity.class, EntityDataSerializers.BOOLEAN);

    public WolfHorseEntity(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ---------- AI ----------

    @Override
    protected void registerGoals() {
        // Deliberately not calling super: we replace the horse brain entirely.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WolfHorseSitGoal(this));
        this.goalSelector.addGoal(4, new WolfHorseMeleeAttackGoal(this, 1.4D, true));
        this.goalSelector.addGoal(5, new WolfHorseFollowOwnerGoal(this, 1.2D, 10.0F, 3.0F));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D, WolfHorseEntity.class));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new WolfHorseOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new WolfHorseOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        // Wild ones hunt, like wild wolves
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Sheep.class, 10, true, false,
                e -> !this.isTamed()));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Rabbit.class, 10, true, false,
                e -> !this.isTamed()));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
    }

    /** Seat height to match the wolf-shaped back (the wolf model is scaled 1.8x). */
    @Override
    public double getPassengersRidingOffset() {
        return 1.4D;
    }

    /** No random grass grazing: wolves don't forage. */
    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        }
        return hit;
    }

    /** Never fight its owner (covers HurtByTargetGoal and all targeting checks). */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isOwner(target) && super.canAttack(target);
    }

    /** Wolf-style rules for what a tamed one is allowed to go after. */
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || target instanceof Ghast || target == owner) {
            return false;
        }
        if (target instanceof WolfHorseEntity other && other.isTamed() && owner.getUUID().equals(other.getOwnerUUID())) {
            return false;
        }
        if (target instanceof Wolf wolf && wolf.isTame() && wolf.getOwner() == owner) {
            return false;
        }
        if (target instanceof Player targetPlayer && owner instanceof Player ownerPlayer
                && !ownerPlayer.canHarmPlayer(targetPlayer)) {
            return false;
        }
        return !(target instanceof AbstractHorse horse && horse.isTamed());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.level().isClientSide) {
            this.setOrderedToSit(false); // wolves stand up when hit
        }
        return super.hurt(source, amount);
    }

    // ---------- Owner helpers ----------

    @Nullable
    public LivingEntity getOwnerEntity() {
        UUID id = this.getOwnerUUID();
        return id == null ? null : this.level().getPlayerByUUID(id);
    }

    public boolean isOwner(LivingEntity entity) {
        UUID id = this.getOwnerUUID();
        return id != null && id.equals(entity.getUUID());
    }

    // ---------- Sitting ----------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SITTING, false);
    }

    public boolean isOrderedToSit() {
        return this.entityData.get(DATA_SITTING);
    }

    public void setOrderedToSit(boolean sit) {
        this.entityData.set(DATA_SITTING, sit);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sitting", this.isOrderedToSit());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setOrderedToSit(tag.getBoolean("Sitting"));
    }

    // ---------- Interaction ----------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;

        // Wild: only bones do anything (no "breaking in" by riding).
        if (!this.isTamed()) {
            if (stack.is(Items.BONE)) {
                if (!client) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (this.random.nextInt(3) == 0
                            && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tameWithName(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7); // hearts
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // smoke
                    }
                }
                return InteractionResult.sidedSuccess(client);
            }
            return InteractionResult.PASS;
        }

        // Tamed: meat heals it, or puts it in love mode for breeding.
        if (this.isFood(stack)) {
            if (this.getHealth() < this.getMaxHealth()) {
                if (!client) {
                    FoodProperties food = stack.getFoodProperties(this);
                    this.heal(food != null ? food.getNutrition() : 2.0F);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT, this);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (!this.isBaby() && this.getAge() == 0 && this.canFallInLove()) {
                if (!client) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    this.setInLove(player);
                }
                return InteractionResult.sidedSuccess(client);
            }
        }

        // Tamed + owner + bone: sit / stand toggle.
        if (stack.is(Items.BONE) && this.isOwner(player) && !player.isSecondaryUseActive()) {
            if (!client) {
                boolean sit = !this.isOrderedToSit();
                this.setOrderedToSit(sit);
                this.navigation.stop();
                this.setTarget(null);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        sit ? "message.wolfhorse.sit" : "message.wolfhorse.stand"), true);
            }
            return InteractionResult.sidedSuccess(client);
        }

        // Everything else (saddle, armor, sneak for inventory, riding) behaves like a horse.
        return super.mobInteract(player, hand);
    }

    /** Only the owner can ride it. */
    @Override
    protected void doPlayerRide(Player player) {
        if (this.isOwner(player)) {
            this.setOrderedToSit(false);
            super.doPlayerRide(player);
        }
    }

    // ---------- Breeding ----------

    @Override
    public boolean isFood(ItemStack stack) {
        FoodProperties food = stack.getFoodProperties(this);
        return food != null && food.isMeat();
    }

    @Override
    public boolean canMate(Animal other) {
        return other != this && other instanceof WolfHorseEntity mate
                && mate.getType() == this.getType()   // Forest with Forest, Tundra with Tundra...
                && this.isTamed() && mate.isTamed()
                && this.canParent() && mate.canParent();
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WolfHorseEntity baby = ((EntityType<WolfHorseEntity>) this.getType()).create(level);
        if (baby != null) {
            baby.rollBreedCoat();
            if (this.getOwnerUUID() != null) {
                baby.setOwnerUUID(this.getOwnerUUID());
                baby.setTamed(true);
            }
            this.setOffspringAttributes(partner, baby);
        }
        return baby;
    }

    // ---------- Breeds (Forest / Plains / Tundra) ----------

    public DireWolfBreed getBreed() {
        if (this.getType() == WolfHorseMod.FOREST_DIREWOLF.get()) return DireWolfBreed.FOREST;
        if (this.getType() == WolfHorseMod.TUNDRA_DIREWOLF.get()) return DireWolfBreed.TUNDRA;
        return DireWolfBreed.PLAINS;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData groupData,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData, tag);
        this.rollBreedCoat();
        return data;
    }

    /** Picks a coat colour and markings from this breed's colour scheme. */
    public void rollBreedCoat() {
        DireWolfBreed breed = this.getBreed();
        this.setCoat(breed.randomCoat(this.random), breed.randomMarkings(this.random));
    }

    private void setCoat(Variant coat, Markings markings) {
        // Horse only exposes coat+markings together through its save data, so round-trip it.
        CompoundTag tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        tag.putInt("Variant", (coat.getId() & 255) | ((markings.getId() & 255) << 8));
        this.readAdditionalSaveData(tag);
    }

    /** Like normal animal spawning, but also allows snow, podzol and dirt (for tundra and taiga). */
    public static boolean checkDirewolfSpawnRules(EntityType<WolfHorseEntity> type, LevelAccessor level,
                                                  MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.SNOW) || below.is(Blocks.POWDER_SNOW)
                || below.is(Blocks.PODZOL) || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.DIRT);
        return ground && Animal.isBrightEnoughToSpawn(level, pos);
    }
}
