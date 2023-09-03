package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.pollen.api.util.NbtConstants;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CreeperSpores extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> LANDED = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CLOUD_SIZE = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.INT);

    private Creepie spawnCreepie;
    private boolean creeperOwner;
    @Nullable
    private LivingEntity target;
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

    private static LivingEntity updateTarget(Mob owner) {
        LivingEntity lastHurt = owner.getLastHurtMob();
        LivingEntity lastHurtBy = owner.getLastHurtByMob();
        if (lastHurt == null && lastHurtBy == null) {
            return null;
        }

        if (lastHurt == null) {
            return lastHurtBy;
        }

        if (lastHurtBy == null) {
            return lastHurt;
        }

        long lastHurtTime = owner.getLastHurtMobTimestamp();
        long lastHurtByTime = owner.getLastHurtByMobTimestamp();
        return lastHurtByTime >= lastHurtTime ? lastHurtBy : lastHurt;
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.creeperOwner = entity instanceof Creeper;
        this.target = entity instanceof Mob mob ? updateTarget(mob) : null;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        boolean landed = this.hasLanded();
        if (hitResult.getType() != HitResult.Type.BLOCK || (!((BlockHitResult) hitResult).isInside() && ((BlockHitResult) hitResult).getDirection() == Direction.UP)) {
            this.onGround = true;
            this.setLanded();
        }

        this.setDeltaMovement(Vec3.ZERO);
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
                this.cloudTime = 20 * cloudSize + 200; // 10 seconds above cloud size
            }

            this.setPos(hitResult.getLocation());
        }
    }

    @Override
    public void tick() {
        if (!this.onGround)
            super.tick();

        if (!this.hasLanded()) {
            this.level.addParticle(TCParticles.CREEPER_SPORES.get(), true, this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f, 0.0f);
        } else {
            if (this.getCloudSize() <= 0)
                this.discard();
            if (this.level.isClientSide()) {
                this.spawnParticleSphere(this.position(), 1, this.getCloudSize());
            } else {
                this.cloudTime--;
                if (this.cloudTime <= 0) {
                    this.discard();
                    return;
                }

                if (this.spawnCreepie == null && this.cloudTime % 10 == 0) {
                    int cloudSize = this.getCloudSize();
                    this.spawnCreepie = null;
                    for (int i = 0; i < 4 * cloudSize; i++) {
                        float theta = (float) (this.random.nextFloat() * 2 * Math.PI);
                        float phi = (float) (this.random.nextFloat() * 2 * Math.PI);

                        double xPos = this.getX() + Mth.sin(phi) * Mth.cos(theta) * cloudSize * this.random.nextFloat();
                        double zPos = this.getZ() + Mth.cos(phi) * cloudSize * this.random.nextFloat();
                        double yPos = this.getY();

                        if (this.level.clip(new ClipContext(this.position(), new Vec3(xPos, yPos, zPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS) {
                            Vec3 creepieSpawnPos = new Vec3(xPos, yPos, zPos);
                            this.spawnCreepie = new Creepie(this.level, this.getOwner(), this.isPowered());
                            this.spawnCreepie.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this.target);
                            if (this.creeperOwner) {
                                this.spawnCreepie.setType(Creepie.CreepieType.NORMAL);
                            }
                            this.spawnCreepie.setPos(creepieSpawnPos);

                            this.spawnParticleSphere(creepieSpawnPos, 50, 1.5F);
                            break;
                        }
                    }
                }
                if (this.spawnCreepie != null && this.cloudTime % 20 == 0) {
                    if (this.level.noCollision(this.spawnCreepie, this.spawnCreepie.getBoundingBox())) {
                        this.level.addFreshEntity(this.spawnCreepie);
                        this.setCloudSize(this.getCloudSize() - 1);
                    }
                    this.spawnCreepie = null;
                }
            }
        }
    }

    private void spawnParticleSphere(Vec3 pos, int amount, float cloudSize) {
        spawnParticleSphere(this, this.random, pos, amount, cloudSize);
    }

    public static void spawnParticleSphere(Entity entity, Random random, Vec3 pos, int amount, float cloudSize) {
        for (int i = 0; i < 4 * amount * cloudSize; i++) {
            float theta = (float) (random.nextFloat() * 2 * Math.PI);
            float phi = (float) (random.nextFloat() * 2 * Math.PI);

            double xPos = Mth.sin(phi) * Mth.cos(theta) * cloudSize * random.nextFloat();
            double yPos = Mth.sin(phi) * Mth.sin(theta) * cloudSize * random.nextFloat();
            double zPos = Mth.cos(phi) * cloudSize * random.nextFloat();

            if (entity.level.clip(new ClipContext(pos, pos.add(xPos, yPos, zPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                if (entity.level.isClientSide()) {
                    entity.level.addParticle(TCParticles.CREEPER_SPORES.get(), true, pos.x() + xPos, pos.y() + yPos, pos.z() + zPos, 0.0D, 0.0D, 0.0D);
                } else {
                    ((ServerLevel) entity.level).sendParticles(TCParticles.CREEPER_SPORES.get(), pos.x() + xPos, pos.y() + yPos, pos.z() + zPos, 1, 0.0D, 0.0D, 0.0D, 0.0);
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
    protected float getGravity() {
        return (this.hasLanded() ? 0.06F : 1.0F) * super.getGravity();
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
