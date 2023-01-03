package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import gg.moonflower.pollen.api.util.NbtConstants;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationEffectHandler;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationState;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import gg.moonflower.tolerablecreepers.core.mixin.CreeperAccessor;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Ocelot
 */
public class Creepie extends Creeper implements AnimatedEntity {

    public static final AnimationState IDLE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"));
    public static final AnimationState IDLE_NOVELTY = new AnimationState(40, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle_novelty1"));
    public static final AnimationState WALK = new AnimationState(0, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_walk"));
    public static final AnimationState HURT = new AnimationState(10, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_hurt"));
    public static final AnimationState SAD = new AnimationState(40, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_simple_walk"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_sad"));
    public static final AnimationState HIDE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_hide"));
    public static final AnimationState DANCE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_dance"));
    private static final AnimationState[] ANIMATIONS = new AnimationState[]{IDLE, IDLE_NOVELTY, HURT, SAD, HIDE, DANCE};

    /**
     * The maximum distance a creeper can be from a creepie before it becomes sad.
     */
    public static final double CREEPER_DISTANCE = 32.0D;
    /**
     * After 6000 ticks (2 minutes) of a creepie being sad, it disappears.
     */
    public static final int MAXIMUM_SAD_TIME = 6000;
    /**
     * The distance to check for jukeboxes and spore blossoms.
     */
    public static final float PARTY_DISTANCE = 8.0F;

    protected static final ImmutableList<SensorType<? extends Sensor<? super Creepie>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY,
            TCEntities.CREEPIE_ATTACKABLES_SENSOR.get(),
            TCEntities.CREEPIE_SPECIFIC_SENSOR.get(),
            TCEntities.CREEPIE_FRIEND_SENSOR.get()
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.VISIBLE_VILLAGER_BABIES,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.CELEBRATE_LOCATION,
            MemoryModuleType.DANCING,
            MemoryModuleType.NEAREST_REPELLENT,
            TCEntities.HIDING_SPOT.get(),
            TCEntities.HAS_FRIENDS.get()
    );

    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Creepie.class, EntityDataSerializers.INT);

    private final AnimationEffectHandler effectHandler;
    private AnimationState animationState;
    private AnimationState transitionAnimationState;
    private int animationTick;
    private int animationTransitionTick;
    private int animationTransitionLength;

    private int age;
    private int forcedAge;
    private int forcedAgeTimer;
    private int sadTimer;
    private int noveltyTimer;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    public Creepie(EntityType<? extends Creepie> entityType, Level level) {
        super(entityType, level);
        this.effectHandler = new AnimationEffectHandler(this);
        this.animationState = AnimationState.EMPTY;
        this.transitionAnimationState = AnimationState.EMPTY;
        this.age = -24000;
        ((CreeperAccessor) this).setExplosionRadius(1);
    }

    public Creepie(Level level, @Nullable Entity owner, boolean powered) {
        this(TCEntities.CREEPIE.get(), level);
        this.setOwner(owner);
        ((CreeperExtension) this).tolerablecreepers$setPowered(powered);
        this.updateState();
    }

    @Override
    protected void registerGoals() {
//        this.goalSelector.addGoal(1, new FloatGoal(this));
//        this.goalSelector.addGoal(2, new SwellGoal(this));
//        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
//        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
//        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
//        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
//        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
//        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
//        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
//        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.345);
    }

    private void updateState() {
        Entity owner = this.getOwner();
        if (owner != null) {
            this.setType(owner instanceof Player ? CreepieType.FRIENDLY : CreepieType.NORMAL);
        } else {
            this.setType(CreepieType.ENRAGED);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.BONE_MEAL)) {
            if (!player.isCreative())
                stack.shrink(1);
            this.ageUp((int) ((float) (-this.getAge() / 20) * 0.1F), true);
            this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
            return InteractionResult.sidedSuccess(this.level.isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide()) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0)
                    this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
                --this.forcedAgeTimer;
            }
        } else if (this.isAlive() && this.getAge() < 0) {
            this.setAge(this.getAge() + 1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        AnimatedEntity.super.animationTick();
        if (!this.isAnimationPlaying(SAD) && !this.isAnimationPlaying(DANCE) && !this.isAnimationTransitioning()) {
            boolean hiding = this.isHiding();
            if (this.level.isClientSide()) {
                if (hiding) {
                    if (!this.isAnimationPlaying(HIDE)) {
                        this.setAnimationState(HIDE, 3);
                    }
                    return;
                } else if (this.isAnimationPlaying(HIDE)) {
                    this.setAnimationState(AnimationState.EMPTY, 3);
                    return;
                }

                if ((!this.isPassenger() && this.isAlive() ? Math.min(this.animationSpeed, 1.0F) : 0.0F) > 1E-6) {
                    if (!this.isNoAnimationPlaying()) {
                        this.setAnimationState(AnimationState.EMPTY);
                    }
                } else if (this.isNoAnimationPlaying()) {
                    this.setAnimationState(IDLE);
                }
            } else if (!hiding && this.isNoAnimationPlaying() && (!this.isPassenger() && this.isAlive() ? Math.min(this.animationSpeed, 1.0F) : 0.0F) <= 1E-6) {
                if (this.noveltyTimer <= 0) {
                    this.noveltyTimer = (2 + this.random.nextInt(9)) * 20; // Every 2-10 seconds, do the novelty animation
                }
                if (--this.noveltyTimer <= 0) { // Subtract before compare
                    AnimatedEntity.setAnimation(this, IDLE_NOVELTY);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("creepieBrain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        CreepieAi.updateActivity(this);

        if (this.sadTimer > 0) {
            this.sadTimer--;
            if (this.sadTimer <= 0) {
                AnimatedEntity.setAnimation(this, SAD);
                this.getNavigation().stop();
                return;
            }
        }

        super.customServerAiStep();
    }

    @Override
    public float getRenderAnimationTick(float partialTicks) {
        if (!this.isNoAnimationPlaying())
            return AnimatedEntity.super.getRenderAnimationTick(partialTicks);
        if (!this.isPassenger() && this.isAlive())
            return (this.animationPosition - this.animationSpeed * (1.0F - partialTicks)) * 6.0F;
        return 0.0F;
    }

    @Override
    public void resetAnimationState(int duration) {
        if (this.animationState == SAD) {
            if (!this.level.isClientSide()) {
                CreeperSpores.spawnParticleSphere(this, this.random, this.position(), 50, 1.5F);
                this.discard();
            }
        }

        AnimatedEntity.super.resetAnimationState(duration);
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        this.animationTick = tick;
    }

    @Override
    public int getAnimationTransitionTick() {
        return animationTransitionTick;
    }

    @Override
    public void setAnimationTransitionTick(int animationTransitionTick) {
        this.animationTransitionTick = animationTransitionTick;
    }

    @Override
    public int getAnimationTransitionLength() {
        return animationTransitionLength;
    }

    @Override
    public void setAnimationTransitionLength(int animationTransitionLength) {
        this.animationTransitionLength = animationTransitionLength;
    }

    @Override
    public AnimationState getAnimationState() {
        return animationState;
    }

    @Override
    public AnimationState getTransitionAnimationState() {
        return transitionAnimationState;
    }

    @Override
    public void setAnimationState(AnimationState state) {
        if (this.animationState == SAD) // Ignore new animations if currently sad
            return;
        this.onAnimationStop(this.animationState);
        this.animationState = state;
        this.setAnimationTick(0);
        this.setAnimationTransitionLength(0);
    }

    @Override
    public void setTransitionAnimationState(AnimationState state) {
        this.transitionAnimationState = state;
    }

    @Override
    public AnimationEffectHandler getAnimationEffects() {
        return effectHandler;
    }

    @Override
    public AnimationState getIdleAnimationState() {
        return WALK;
    }

    @Override
    public AnimationState[] getAnimationStates() {
        return ANIMATIONS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.ownerUUID != null)
            nbt.putUUID("Owner", this.ownerUUID);
        nbt.putString("Type", this.getCreepieType().name().toLowerCase(Locale.ROOT));
        nbt.putInt("Age", this.getAge());
        nbt.putInt("ForcedAge", this.forcedAge);
        nbt.putInt("SadTime", this.sadTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.ownerUUID = nbt.hasUUID("Owner") ? nbt.getUUID("Owner") : null;
        this.setType(CreepieType.byName(nbt.getString("Type")));
        this.setAge(nbt.contains("Age", NbtConstants.ANY_NUMERIC) ? nbt.getInt("Age") : -24000);
        this.forcedAge = nbt.getInt("ForcedAge");
        this.sadTimer = nbt.getInt("SadTime");
    }

    public void setOwner(@Nullable Entity entity) {
        this.ownerUUID = entity != null ? entity.getUUID() : null;
        this.cachedOwner = entity;
        this.updateState();
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            return this.cachedOwner = ((ServerLevel) this.level).getEntity(this.ownerUUID);
        } else {
            return null;
        }
    }

    public void setType(CreepieType type) {
        this.entityData.set(DATA_TYPE_ID, type.ordinal());
    }

    public CreepieType getCreepieType() {
        int type = this.entityData.get(DATA_TYPE_ID);
        if (type < 0 || type >= CreepieType.values().length) {
            this.entityData.set(DATA_TYPE_ID, 0);
            return CreepieType.NORMAL;
        }
        return CreepieType.values()[type];
    }

    public void ageUp(int amount, boolean forceAge) {
        int j = this.getAge();
        j += amount * 20;
        if (j > 0) {
            j = 0;
        }

        this.setAge(j);
        if (forceAge) {
            if (this.forcedAgeTimer == 0) {
                this.forcedAgeTimer = 40;
            }
        }

        if (this.getAge() == 0)
            this.setAge(this.forcedAge);
    }

    public int getAge() {
        return this.level.isClientSide() ? -1 : this.age;
    }

    public void setAge(int age) {
        this.age = age;
        if (!this.level.isClientSide() && age >= 0)
            this.convertTo(EntityType.CREEPER, false);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            float f = this.getEquipmentDropChance(equipmentSlot);
            boolean bl2 = f > 1.0F;
            if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && (bl || bl2) && Math.max(this.random.nextFloat() - (float) i * 0.01F, 0.0F) < f) {
                if (!bl2 && itemStack.isDamageableItem()) {
                    itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
                }

                this.spawnAtLocation(itemStack);
                this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TCItems.CREEPER_SPORES.get());
    }

    @Override
    protected Brain.Provider<Creepie> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CreepieAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<Creepie> getBrain() {
        return (Brain<Creepie>) super.getBrain();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data, @Nullable CompoundTag nbt) {
        CreepieAi.initMemories(this);
        return super.finalizeSpawn(level, difficulty, type, data, nbt);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean bl = super.hurt(source, amount);
        if (bl && !this.level.isClientSide()) {
            AnimatedEntity.setAnimation(this, HURT);
            if (source.getEntity() instanceof LivingEntity livingAttacker) {
                CreepieAi.wasHurtBy(this, livingAttacker);
            }
        }
        return bl;
    }

    protected void playSound(SoundEvent soundEvent) {
        this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
    }

    public void setSad(boolean sad) {
        this.sadTimer = sad ? MAXIMUM_SAD_TIME : 0;
    }

    public boolean isDancing() {
        return this.getAnimationState() == DANCE;
    }

    public boolean isSad() {
        return this.sadTimer > 0 || this.isAnimationPlaying(SAD);
    }

    public boolean canMove() {
        return this.isNoAnimationPlaying();
    }

    public boolean isHiding() {
        return this.brain.hasMemoryValue(TCEntities.HIDING_SPOT.get()) || this.level.getBlockState(this.blockPosition()).is(TCTags.CREEPIE_HIDING_SPOTS);
    }

    public enum CreepieType {
        NORMAL, ENRAGED, FRIENDLY;

        private final ResourceLocation texture;

        CreepieType() {
            this.texture = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_" + this.name().toLowerCase(Locale.ROOT));
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public static CreepieType byName(String name) {
            for (CreepieType type : values())
                if (type.name().toLowerCase(Locale.ROOT).equals(name))
                    return type;
            return NORMAL;
        }
    }
}
