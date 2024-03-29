package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import gg.moonflower.pinwheel.api.animation.AnimationController;
import gg.moonflower.pollen.api.animation.v1.AnimationRuntime;
import gg.moonflower.pollen.api.animation.v1.controller.AnimationStateListener;
import gg.moonflower.pollen.api.animation.v1.controller.IdleAnimationController;
import gg.moonflower.pollen.api.animation.v1.controller.StateAnimationController;
import gg.moonflower.pollen.api.animation.v1.entity.AnimatedEntity;
import gg.moonflower.pollen.api.animation.v1.state.AnimationState;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import gg.moonflower.tolerablecreepers.core.mixin.CreeperAccessor;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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
public class Creepie extends Creeper implements AnimatedEntity, AnimationStateListener {

    private static final ResourceLocation SETUP_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_setup");
    private static final ResourceLocation IDLE_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle");
    private static final ResourceLocation IDLE_NOVELTY_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle_novelty1");
    private static final ResourceLocation WALK_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_walk");
    private static final ResourceLocation HURT_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_hurt");
    private static final ResourceLocation SIMPLE_WALK_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_simple_walk");
    private static final ResourceLocation SAD_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_sad");
    private static final ResourceLocation HIDE_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_hide");
    private static final ResourceLocation DANCE_ANIMATION = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_dance");

    public static final AnimationState IDLE = new AnimationState(0, SETUP_ANIMATION, IDLE_ANIMATION);
    public static final AnimationState IDLE_NOVELTY = new AnimationState(40, SETUP_ANIMATION, IDLE_NOVELTY_ANIMATION);
    public static final AnimationState WALK = new AnimationState(0, SETUP_ANIMATION, IDLE_ANIMATION, WALK_ANIMATION);
    public static final AnimationState HURT = new AnimationState(10, SETUP_ANIMATION, HURT_ANIMATION);
    public static final AnimationState SAD = new AnimationState(40, SETUP_ANIMATION, SIMPLE_WALK_ANIMATION, SAD_ANIMATION);
    public static final AnimationState HIDE = new AnimationState(Integer.MAX_VALUE, SETUP_ANIMATION, HIDE_ANIMATION);
    public static final AnimationState DANCE = new AnimationState(Integer.MAX_VALUE, SETUP_ANIMATION, DANCE_ANIMATION);
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
    private static final AnimationState[] ANIMATIONS = new AnimationState[]{IDLE_NOVELTY, HURT, SAD, HIDE, DANCE};
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Creepie.class, EntityDataSerializers.INT);

    private final StateAnimationController animationController;
    private final IdleAnimationController renderAnimationController;

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
        this.animationController = AnimationRuntime.createState(ANIMATIONS, this);
        this.animationController.addListener(this);
        this.renderAnimationController = AnimationRuntime.createIdle(this.animationController, WALK.animations());
        this.renderAnimationController.setRenderTimer(WALK_ANIMATION, (playingAnimation, lastTime, partialTicks) -> {
            if (!this.isPassenger() && this.isAlive()) {
                return (this.animationPosition - this.animationSpeed * (1.0F - partialTicks)) / 4.0F;
            }
            return 0.0F;
        });
        this.age = -24000;
        ((CreeperAccessor) this).setExplosionRadius(1);
    }

    public Creepie(Level level, @Nullable Entity owner, boolean powered) {
        this(TCEntities.CREEPIE.get(), level);
        this.setOwner(owner);
        ((CreeperExtension) this).tolerablecreepers$setPowered(powered);
        this.updateState();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.345);
    }

    public void setPlayingAnimation(AnimationState state, int transitionTicks) {
        if (!this.animationController.isAnimationPlaying(SAD)) {// Ignore new animations if currently sad
            this.animationController.setPlayingAnimation(state, transitionTicks);
        }
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
            this.gameEvent(GameEvent.ENTITY_INTERACT);
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
        this.animationController.tick();
        if (this.level.isClientSide()) {
            this.renderAnimationController.tick();
        } else {
            this.syncClient();
        }
        if (!this.animationController.isAnimationPlaying(SAD) && !this.animationController.isAnimationPlaying(DANCE)) {
            boolean hiding = this.isHiding();
            boolean walking = !this.isPassenger() && this.isAlive() && Math.min(this.animationSpeed, 1.0F) > 1E-6;
            if (this.level.isClientSide()) {
                if (hiding) {
                    if (!this.animationController.isAnimationPlaying(HIDE)) {
                        this.animationController.setPlayingAnimation(HIDE); // TODO 3 ticks of transition
                    }
                    return;
                } else if (this.animationController.isAnimationPlaying(HIDE)) {
                    this.animationController.stopAnimations(HIDE); // TODO 3 ticks of transition
                    return;
                }

                this.animationController.clearAnimations();
                this.renderAnimationController.setIdleAnimations(walking ? WALK.animations() : IDLE.animations());
            } else if (!hiding && !walking && this.animationController.isNoAnimationPlaying()) {
                if (this.noveltyTimer <= 0) {
                    this.noveltyTimer = (2 + this.random.nextInt(9)) * 20; // Every 2-10 seconds, do the novelty animation
                }
                if (--this.noveltyTimer <= 0) { // Subtract before compare
                    this.setPlayingAnimation(IDLE_NOVELTY, 1);
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
                this.setPlayingAnimation(SAD, 1);
                this.getNavigation().stop();
                return;
            }
        }

        super.customServerAiStep();
    }

    @Override
    public void onAnimationStop(AnimationState state) {
        if (!this.level.isClientSide() && state == SAD) {
            CreeperSpores.spawnParticleSphere(this, this.random, this.position(), 50, 1.5F);
            this.discard();
        }
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
        this.setAge(nbt.contains("Age", Tag.TAG_ANY_NUMERIC) ? nbt.getInt("Age") : -24000);
        this.forcedAge = nbt.getInt("ForcedAge");
        this.sadTimer = nbt.getInt("SadTime");
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

    public void setOwner(@Nullable Entity entity) {
        this.ownerUUID = entity != null ? entity.getUUID() : null;
        this.cachedOwner = entity;
        this.updateState();
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
    protected PathNavigation createNavigation(Level level) {
        PathNavigation navigation = super.createNavigation(level);
        navigation.setCanFloat(true);
        return navigation;
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
            this.animationController.setPlayingAnimation(HURT);
            if (source.getEntity() instanceof LivingEntity livingAttacker) {
                CreepieAi.wasHurtBy(this, livingAttacker);
            }
        }
        return bl;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public void playSound(SoundEvent soundEvent) {
        this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
    }

    public boolean isDancing() {
        return this.animationController.isAnimationPlaying(DANCE);
    }

    public boolean isSad() {
        return this.sadTimer > 0 || this.animationController.isAnimationPlaying(SAD);
    }

    public void setSad(boolean sad) {
        this.sadTimer = sad ? MAXIMUM_SAD_TIME : 0;
    }

    public boolean canMove() {
        return this.animationController.getPlayingAnimations().isEmpty();
    }

    public boolean isHiding() {
        return this.brain.hasMemoryValue(TCEntities.HIDING_SPOT.get()) || this.level.getBlockState(this.blockPosition()).is(TCTags.CREEPIE_HIDING_SPOTS);
    }

    @Override
    public AnimationController getAnimationController() {
        return this.animationController;
    }

    @Override
    public AnimationController getRenderAnimationController() {
        return this.renderAnimationController;
    }

    public enum CreepieType {
        NORMAL, ENRAGED, FRIENDLY;

        private final ResourceLocation texture;

        CreepieType() {
            this.texture = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_" + this.name().toLowerCase(Locale.ROOT));
        }

        public static CreepieType byName(String name) {
            for (CreepieType type : values())
                if (type.name().toLowerCase(Locale.ROOT).equals(name))
                    return type;
            return NORMAL;
        }

        public ResourceLocation getTexture() {
            return texture;
        }
    }
}
