package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationEffectHandler;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationState;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author Ocelot
 */
public class Creepie extends Creeper implements AnimatedEntity {

    public static final AnimationState IDLE = new AnimationState(0, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_walk"));
    public static final AnimationState DANCE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_dance"));
    private static final AnimationState[] ANIMATIONS = new AnimationState[]{DANCE};

    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(Creepie.class, EntityDataSerializers.BOOLEAN);

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
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.345);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANGRY, false);
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
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null)
            compoundTag.putUUID("Owner", this.ownerUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner"))
            this.ownerUUID = compoundTag.getUUID("Owner");
        this.entityData.set(ANGRY, this.ownerUUID == null);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
        this.entityData.set(ANGRY, entity == null);
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

    public boolean isAngry() {
        return this.level.isClientSide() ? this.entityData.get(ANGRY) : this.ownerUUID == null;
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
}
