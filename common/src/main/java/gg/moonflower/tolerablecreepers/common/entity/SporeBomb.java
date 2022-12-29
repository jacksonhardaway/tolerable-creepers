package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SporeBomb extends ThrowableBomb {

    public SporeBomb(EntityType<? extends ThrowableBomb> entityType, Level level) {
        super(entityType, level);
    }

    public SporeBomb(LivingEntity livingEntity, Level level) {
        super(TCEntities.SPORE_BOMB.get(), livingEntity, level);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1.0, 0.0, 0.0);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void explode() {
        CreeperSpores spores = new CreeperSpores(this.level, this.getX(), this.getY() + 0.01, this.getZ(), 1 + this.random.nextInt(2), false);
        if (!(this.getOwner() instanceof LivingEntity livingEntity) || !livingEntity.hasEffect(MobEffects.INVISIBILITY)) {
            spores.setOwner(this.getOwner());
        }
        this.level.addFreshEntity(spores);
        this.level.broadcastEntityEvent(this, (byte) (this.isInWater() ? 1 : 0));
        this.discard();
    }

    @Override
    protected ParticleOptions getParticle() {
        return TCParticles.CREEPER_SPORES.get();
    }
}
