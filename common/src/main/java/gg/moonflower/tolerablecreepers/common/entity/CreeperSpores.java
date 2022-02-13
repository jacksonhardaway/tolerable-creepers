package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.pollen.api.util.NbtConstants;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CreeperSpores extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> LANDED = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CLOUD_SIZE = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.INT);

    private final BlockPos.MutableBlockPos particlePos = new BlockPos.MutableBlockPos();
    private int cloudTime;

    public CreeperSpores(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(CLOUD_SIZE, 2);
    }

    public CreeperSpores(Level level, double x, double y, double z, int cloudSize, boolean powered) {
        super(TCEntities.CREEPER_SPORES.get(), x, y, z, level);
        this.entityData.set(CLOUD_SIZE, cloudSize);
        this.entityData.set(POWERED, powered);
    }

    public CreeperSpores(LivingEntity thrower, Level level, int cloudSize) {
        super(TCEntities.CREEPER_SPORES.get(), thrower, level);
        this.entityData.set(CLOUD_SIZE, cloudSize);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        boolean landed = this.hasLanded();
        if (hitResult != null) {
            if (hitResult.getType() != HitResult.Type.BLOCK || !((BlockHitResult) hitResult).isInside() || ((BlockHitResult) hitResult).getDirection() == Direction.UP)
                this.setLanded();
            this.setPos(hitResult.getLocation());
        }

        if (landed != this.hasLanded()) {
            int cloudSize = this.getCloudSize();
            if (this.level.isClientSide()) {
                for (int i = 0; i < 30 * cloudSize; i++) {
                    double theta = this.random.nextFloat() * 2 * Math.PI;
                    double xVelocity = Math.cos(theta) * (this.random.nextFloat() * 0.3 + 0.7) * cloudSize;
                    double zVelocity = Math.sin(theta) * (this.random.nextFloat() * 0.3 + 0.7) * cloudSize;
                    this.level.addParticle(TCParticles.CREEPER_SPORES.get(), false, this.getX(), this.getY(), this.getZ(), xVelocity, this.random.nextFloat() * 0.2, zVelocity);
                }
            } else {
                this.cloudTime = 20 * cloudSize;
            }

            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void tick() {
        if (!this.hasLanded())
            super.tick();

        if (!this.hasLanded()) {
            this.level.addParticle(TCParticles.CREEPER_SPORES.get(), true, this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f, 0.0f);
        } else {
            if (this.level.isClientSide()) {
                int cloudSize = this.getCloudSize();
                for (int i = 0; i < 4 * cloudSize; i++) {
                    float theta = (float) (this.random.nextFloat() * 2 * Math.PI);
                    float phi = (float) (this.random.nextFloat() * 2 * Math.PI);

                    double xPos = this.getX() + Mth.sin(phi) * Mth.cos(theta) * cloudSize * this.random.nextFloat();
                    double yPos = this.getY() + Mth.sin(phi) * Mth.sin(theta) * cloudSize * this.random.nextFloat();
                    double zPos = this.getZ() + Mth.cos(phi) * cloudSize * this.random.nextFloat();

                    if (this.level.clip(new ClipContext(this.position(), new Vec3(xPos, yPos, zPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS)
                        this.level.addParticle(TCParticles.CREEPER_SPORES.get(), true, xPos, yPos, zPos, 0.0D, 0.0D, 0.0D);
                }
            } else {
                this.cloudTime--;
                if (this.cloudTime <= 0) {
                    this.discard();
                    return;
                }

                if (this.cloudTime % 10 == 0) {
                    //Add zooming particles to new locations if applicable
                }
                if (this.cloudTime % 20 == 0) {
                    /*TODO creepie spawning
                     * Looks for spawning spots where the block's hitbox doesn't intersect creepie's
                     * Spawns one there
                     * Spawns some particles zooming in to the spawn location (nonessential)
                     */
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LANDED, false);
        this.entityData.define(POWERED, false);
        this.entityData.define(CLOUD_SIZE, 0);
    }

    private boolean hasLanded() {
        return this.entityData.get(LANDED);
    }

    private boolean isPowered() {
        return this.entityData.get(POWERED);
    }

    private int getCloudSize() {
        return this.entityData.get(CLOUD_SIZE);
    }

    private void setLanded() {
        this.entityData.set(LANDED, true);
    }

    private void setCloudSize(int cloudTime) {
        this.entityData.set(CLOUD_SIZE, cloudTime);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("CloudSize", this.getCloudSize());
        nbt.putInt("CloudTime", this.cloudTime);
        nbt.putBoolean("Powered", this.isPowered());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("CloudSize", NbtConstants.ANY_NUMERIC))
            this.setCloudSize(nbt.getInt("CloudSize"));
        if (nbt.contains("CloudTime", NbtConstants.ANY_NUMERIC)) {
            this.cloudTime = nbt.getInt("CloudTime");
            this.setLanded();
        }
        this.entityData.set(POWERED, nbt.getBoolean("Powered"));
    }
}
