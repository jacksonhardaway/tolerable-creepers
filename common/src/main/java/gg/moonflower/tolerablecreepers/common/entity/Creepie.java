package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationEffectHandler;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationState;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ocelot
 */
public class Creepie extends Creeper implements AnimatedEntity {

    public static final AnimationState IDLE = new AnimationState(0, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_walk"));
    public static final AnimationState DANCE = new AnimationState(Integer.MAX_VALUE, new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_idle"), new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_dance"));
    private static final AnimationState[] ANIMATIONS = new AnimationState[]{DANCE};

    private final AnimationEffectHandler effectHandler;
    private int animationTick;
    private AnimationState animationState;

    public Creepie(EntityType<? extends Creepie> entityType, Level level) {
        super(entityType, level);
        this.effectHandler = new AnimationEffectHandler(this);
        this.animationState = AnimationState.EMPTY;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.345);
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
}
