package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PrimedSporeBarrel extends PrimedTnt {

    @Nullable
    private LivingEntity owner;

    public PrimedSporeBarrel(EntityType<? extends PrimedSporeBarrel> entityType, Level level) {
        super(entityType, level);
    }

    public PrimedSporeBarrel(Level level, double d, double e, double f, @Nullable LivingEntity livingEntity) {
        this(TCEntities.SPORE_BARREL.get(), level);
        this.setPos(d, e, f);
        double g = level.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(g) * 0.02, 0.2F, -Math.cos(g) * 0.02);
        this.setFuse(20);
        this.xo = d;
        this.yo = e;
        this.zo = f;
        this.owner = livingEntity;
    }

    @Override
    public void tick() {
        if (!this.isNoGravity())
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround)
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.level.isClientSide()) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level.isClientSide()) {
                this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        this.level.explode(this, this.getX(), this.getY(0.0625), this.getZ(), 4.0F / 3.0F, Explosion.BlockInteraction.NONE);
        CreeperSpores creeperSpores = new CreeperSpores(this.level, this.getX(), this.getY() + 0.01, this.getZ(), this.random.nextInt(7) + 5, false);
        if (this.owner != null && !this.owner.isInvisible())
            creeperSpores.setOwner(this.owner);
        this.level.addFreshEntity(creeperSpores);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return owner;
    }
}
