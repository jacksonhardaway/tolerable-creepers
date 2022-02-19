package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import gg.moonflower.pollen.api.util.NbtConstants;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationEffectHandler;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationState;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Ocelot
 */
public class Creepie extends Creeper implements AnimatedEntity {

    public static final AnimationState IDLE = new AnimationState(0, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_walk"));
    public static final AnimationState DANCE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_dance"));
    private static final AnimationState[] ANIMATIONS = new AnimationState[]{DANCE};

    protected static final ImmutableList<SensorType<? extends Sensor<? super Creepie>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY,
            TCEntities.CREEPIE_ATTACKABLES_SENSOR.get(),
            TCEntities.CREEPIE_SPECIFIC_SENSOR.get()
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
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
            MemoryModuleType.NEAREST_REPELLENT
    );

    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Creepie.class, EntityDataSerializers.INT);

    private final AnimationEffectHandler effectHandler;
    private int animationTick;
    private AnimationState animationState;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    public Creepie(EntityType<? extends Creepie> entityType, Level level) {
        super(entityType, level);
        this.effectHandler = new AnimationEffectHandler(this);
        this.animationState = AnimationState.EMPTY;
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
        this.entityData.define(TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        AnimatedEntity.super.animationTick();
    }

    @Override
    public float getRenderAnimationTick(float partialTicks) {
        if (!this.isNoAnimationPlaying())
            return AnimatedEntity.super.getRenderAnimationTick(partialTicks);
        float o = 0.0F;
        if (!this.isPassenger() && this.isAlive()) {
            o = this.animationPosition - this.animationSpeed * (1.0F - partialTicks);
            if (this.isBaby()) {
                o *= 3.0F;
            }
        }

        return o * 4.0F;
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
    public AnimationState getAnimationState() {
        return animationState;
    }

    @Override
    public AnimationState getIdleAnimationState() {
        return IDLE;
    }

    @Override
    public void setAnimationState(AnimationState state) {
        this.animationState = state;
    }

    @Nullable
    @Override
    public AnimationEffectHandler getAnimationEffects() {
        return effectHandler;
    }

    @Override
    public AnimationState[] getAnimationStates() {
        return ANIMATIONS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        if (this.ownerUUID != null)
            nbt.putUUID("Owner", this.ownerUUID);
        nbt.putString("Type", this.getCreepieType().name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.hasUUID("Owner"))
            this.ownerUUID = nbt.getUUID("Owner");
        if (nbt.contains("Type", NbtConstants.STRING))
            this.setType(CreepieType.byName(nbt.getString("Type")));
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
        this.updateState();
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    private void setType(CreepieType type) {
        this.entityData.set(TYPE, type.ordinal());
    }

    public CreepieType getCreepieType() {
        int type = this.entityData.get(TYPE);
        if (type < 0 || type >= CreepieType.values().length) {
            this.entityData.set(TYPE, 0);
            return CreepieType.NORMAL;
        }
        return CreepieType.values()[type];
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

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("creepieBrain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        CreepieAi.updateActivity(this);
        super.customServerAiStep();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        CreepieAi.initMemories(this);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl = super.hurt(damageSource, f);
        if (bl && !this.level.isClientSide() && damageSource.getEntity() instanceof LivingEntity livingAttacker)
            CreepieAi.wasHurtBy(this, livingAttacker);
        return bl;
    }

    protected void playSound(SoundEvent soundEvent) {
        this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
    }

    public boolean isDancing() {
        return this.getAnimationState() == DANCE;
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
