package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FireBomb extends ThrowableBomb {

    public FireBomb(EntityType<? extends ThrowableBomb> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level.isClientSide() && this.level.getBlockState(this.blockPosition()).is(TCTags.FIRE_BOMB_EXPLODE)) {
            this.explode();
        }
    }

    private void spawnParticles(ParticleOptions particle, int amount) {
        for (int i = 0; i < amount; i++) {
            double theta = this.random.nextFloat() * 2 * Math.PI;
            double alpha = this.random.nextFloat() * 2 * Math.PI;
            double cos = Math.cos(alpha);
            double xVelocity = Math.sin(theta) * cos * (this.random.nextFloat() * 0.3 + 0.7);
            double yVelocity = cos * Math.cos(theta) * (this.random.nextFloat() * 0.3 + 0.7);
            double zVelocity = Math.sin(alpha) * (this.random.nextFloat() * 0.3 + 0.7);
            this.level.addParticle(particle, this.getX(), this.getY(), this.getZ(), xVelocity * 0.6, yVelocity * 0.6, zVelocity * 0.6);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            this.spawnParticles(ParticleTypes.SMOKE, 320);

            for (int i = 0; i < 40; i++) {
                double theta = this.random.nextFloat() * 2 * Math.PI;
                double alpha = this.random.nextFloat() * 2 * Math.PI;
                double cos = Math.cos(alpha);
                double xVelocity = Math.sin(theta) * cos * (this.random.nextFloat() * 0.3 + 0.7);
                double yVelocity = cos * Math.cos(theta) * (this.random.nextFloat() * 0.3 + 0.7);
                double zVelocity = Math.sin(alpha) * (this.random.nextFloat() * 0.3 + 0.7);
                this.level.addParticle(ParticleTypes.FLAME, false, this.getX(), this.getY(), this.getZ(), xVelocity * 0.1, yVelocity * 0.1, zVelocity * 0.1);
            }
        } else if (b == 1) {
            this.spawnParticles(ParticleTypes.SMOKE, 400);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void explode() {
        this.level.explode(this, this.getX(), this.getY(0.0625), this.getZ(), 2.0F, Explosion.BlockInteraction.BREAK);
        // Light nearby campfires on fire
        BlockPos.withinManhattan(this.blockPosition(), 6, 4, 6).forEach(pos -> {
            BlockState state = this.level.getBlockState(pos);
            if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && !state.getValue(CampfireBlock.LIT)) {
                this.level.setBlock(pos, state.setValue(CampfireBlock.LIT, true), 3);
            }
        });
        this.level.broadcastEntityEvent(this, (byte) (this.isInWater() ? 1 : 0));
        this.discard();
    }

    public FireBomb(LivingEntity livingEntity, Level level) {
        super(TCEntities.FIRE_BOMB.get(), livingEntity, level);
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.SMOKE;
    }
}
