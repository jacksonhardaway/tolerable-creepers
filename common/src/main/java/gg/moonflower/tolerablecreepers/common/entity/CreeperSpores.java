package gg.moonflower.tolerablecreepers.common.entity;

import gg.moonflower.tolerablecreepers.client.particle.CreeperSporesParticle;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/*
* This isn't finished, the two most important things are:
* 1. Gravity actually working - it can fall through the floor
* 2. Clouds from creepers not working
* Lower priority:
* 1. position validation
* When creepies are added:
* 1. Add information for who owns the creepies
* 2. Spawn creepies from clouds
* */
public class CreeperSpores extends ThrowableProjectile {
    protected int cloudSize; //cloud size represents the number of creepies and the diameter
    protected Set<BlockPos> blacklistedPositions = new HashSet<>(); //This will be used to stop particles and creepies from spawning in positions that the cloud couldn't reach, it might need to be synced data
    private final EntityDataAccessor<Integer> WARMUP_TIME = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.INT);
    private final EntityDataAccessor<Integer> CLOUD_TIME = SynchedEntityData.defineId(CreeperSpores.class, EntityDataSerializers.INT);
    //These are all data accessors because they need to be updated and used on both server and client

    public CreeperSpores(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.cloudSize = 2;
    }

    public CreeperSpores(Level level, double x, double y, double z, int cloudSize, boolean createCloudInstantly) {
        super(TCEntities.CREEPER_SPORES.get(), x, y, z, level);
        this.cloudSize = cloudSize;
        if (createCloudInstantly) //TODO this doesn't work because it's initialised only by the client, make this a field
            this.createCloud();
    }

    public CreeperSpores(LivingEntity thrower, Level level, int cloudSize, boolean createCloudInstantly) {
        super(TCEntities.CREEPER_SPORES.get(), thrower, level);
        this.cloudSize = cloudSize;
        if (createCloudInstantly)
            this.createCloud();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.getCloudTime() <= 0)
            this.createCloud();
    }

    public void createCloud() {
        double minusLnFriction = -Math.log(CreeperSporesParticle.FRICTION);
        if (this.level.isClientSide()) {
            double baseSpeed = this.cloudSize * minusLnFriction; //The speed needed to travel a certain distance (cloudSize) given that it's multiplied by the friction every tick
            //(minusLnFriction works by integrating the graph of friction and position iirc)
            for (int i = 0; i < 50 * this.cloudSize; i++) {
                double theta = this.random.nextFloat() * 2 * Math.PI;
                double phi = this.random.nextFloat() * 2 * Math.PI;
                //Trig moment
                double xVelocity = (Math.cos(theta) * baseSpeed) * this.random.nextFloat();
                double yVelocity = (Math.sin(theta) * baseSpeed) * this.random.nextFloat();
                double zVelocity = (Math.cos(phi) * baseSpeed) * this.random.nextFloat();
                this.level.addParticle(TCParticles.CREEPER_SPORES.get(), this.getX(), this.getY(), this.getZ(), xVelocity, yVelocity, zVelocity);
            }
        }
        this.setDeltaMovement(new Vec3(0.0D, 0.0D, 0.0D));
        this.setCloudTime(20 * this.cloudSize);
        this.refreshBlacklistedPositions();
        this.setWarmupTime((int) (1 + (1 / minusLnFriction))); //The time it takes for the particles to stop moving, always rounded up
    }

    //TODO stop gravity if blocks are below the cloud
    @Override
    public void tick() {
        super.tick();
            if (getCloudTime() < 0)
                this.level.addParticle(TCParticles.CREEPER_SPORES.get(), this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f, 0.0f);
            else if (this.getWarmupTime() <= 0) {
                this.setCloudTime(this.getCloudTime()-1);
                if (this.getCloudTime() == 0)
                    this.discard();
                if (this.level.isClientSide()) {
                    for (int i = 0; i < 5 * this.cloudSize; i++) {
                        double theta = this.random.nextFloat() * 2 * Math.PI;
                        double phi = this.random.nextFloat() * 2 * Math.PI;

                        double xPos = this.getX() + Math.cos(theta) * this.cloudSize * this.random.nextFloat();
                        double yPos = this.getY() + Math.sin(theta) * this.cloudSize * this.random.nextFloat();
                        double zPos = this.getZ() + Math.cos(phi) * this.cloudSize * this.random.nextFloat();
                        this.level.addParticle(TCParticles.CREEPER_SPORES.get(), xPos, yPos, zPos, 0.0D, 0.0D, 0.0D);
                    }
                } else if (this.getCloudTime() % 10 == 0) {
                    this.refreshBlacklistedPositions();
                    //Add zooming particles to new locations if applicable
                }
                if (this.getCloudTime() % 20 == 0) {
                    /*TODO creepie spawning
                     * Looks for spawning spots where the block's hitbox doesn't intersect creepie's
                     * Spawns one there
                     * Spawns some particles zooming in to the spawn location (nonessential)
                     */
                }
            } else this.setWarmupTime(this.getWarmupTime()-1);
    }

    protected void refreshBlacklistedPositions() {
        BlockPos.MutableBlockPos checkingPos = this.blockPosition().mutable();
        Set<BlockPos> temporaryGreylist = new HashSet<>();
        for (Direction direction : Direction.values()) {
            for (int i = cloudSize; i > 0; i--) {
                for (int j = -((2 * i)+1)/2; j < 2*((2 * i)+1); j++) {
                    /*TODO position validation - prevents particles from appearing in front of, below or above blocks that block the cloud's explosion
                     * 1. For each direction, iterate through 2i+1 blocks from left to right of that direction
                     * 2. Push the turtle forward in that direction for cloudSize number of iterations
                     * 3. If an opaque block is found, add that to the blacklist and all blocks directly next to it in all directions to the temporary greylist
                     * 4. If an entire row of opaque or grey list blocks is found (i.e. the entire j loop) then blacklist all blocks in front of it in the current direction
                     *
                     * Hopefully this actually works when implemented
                     */
                }
            }
        }
    }

    @Override
    protected float getGravity() {
        return (this.getCloudTime() > 0 ? 0.06F : 1.0F) * super.getGravity();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CLOUD_TIME, -1);
        this.entityData.define(WARMUP_TIME, 0); //This isn't saved because it's always really short and relies on
    }

    public int getCloudTime() {
        return this.entityData.get(CLOUD_TIME);
    }

    protected void setCloudTime(int cloudTime) {
        this.entityData.set(CLOUD_TIME, cloudTime);
    }

    public int getWarmupTime() {
        return this.entityData.get(WARMUP_TIME);
    }

    protected void setWarmupTime(int warmupTime) {
        this.entityData.set(WARMUP_TIME, warmupTime);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("CloudSize", this.cloudSize);
        compoundTag.putInt("CloudTime", this.getCloudTime());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CloudSize", 3))
            this.cloudSize = compoundTag.getInt("CloudSize");
        if (compoundTag.contains("CloudTime", 3))
            this.setCloudTime((compoundTag.getInt("CloudTime")));
    }
}
