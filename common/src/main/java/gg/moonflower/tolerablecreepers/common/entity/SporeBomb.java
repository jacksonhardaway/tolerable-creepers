package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class SporeBomb extends ThrowableBomb {

    public SporeBomb(EntityType<? extends ThrowableBomb> entityType, Level level) {
        super(entityType, level);
    }

    public SporeBomb(LivingEntity livingEntity, Level level) {
        super(TCEntities.SPORE_BOMB.get(), livingEntity, level);
    }

    @Override
    protected void explode() {
        CreeperSpores spores = new CreeperSpores(this.level, this.getX(), this.getY() + 0.01, this.getZ(), 1 + this.random.nextInt(2), false);
        if (!(this.getOwner() instanceof LivingEntity livingEntity) || !livingEntity.hasEffect(MobEffects.INVISIBILITY)) {
            spores.setOwner(this.getOwner());
        }
        this.level.addFreshEntity(spores);
        this.level.gameEvent(this, GameEvent.EXPLODE, this);
        this.level.playSound(null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F
        );
        this.discard();
    }

    @Override
    protected ParticleOptions getParticle() {
        return TCParticles.CREEPER_SPORES.get();
    }
}
