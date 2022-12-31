package gg.moonflower.tolerablecreepers.common.entity.ai.sensing;

import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public class CreepieAttackablesSensor extends NearestVisibleLivingEntitySensor {

    public static final float TARGET_DETECTION_DISTANCE = 8.0F;

    @Override
    protected boolean isMatchingEntity(LivingEntity entity, LivingEntity target) {
        return entity instanceof Creepie creepie && this.isClose(entity, target) && this.isTarget(creepie, target) && Sensor.isEntityAttackable(entity, target);
    }

    private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
        return livingEntity2.distanceToSqr(livingEntity) <= TARGET_DETECTION_DISTANCE * TARGET_DETECTION_DISTANCE;
    }

    private boolean isTarget(Creepie creepie, LivingEntity target) {
        if (target.getType().is(TCTags.CREEPIE_AVOID)) {
            return false;
        }
        if (creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY && creepie.getOwner() != null) {
            return false;
        }
        if (target instanceof Creepie targetCreepie && targetCreepie.getCreepieType() == creepie.getCreepieType()) {
            return false;
        }
        if (creepie.getCreepieType() == Creepie.CreepieType.ENRAGED) {
            return !(target instanceof Creepie targetCreepie) || targetCreepie.getCreepieType() != Creepie.CreepieType.ENRAGED;
        }
        return target instanceof Player || target instanceof IronGolem || target instanceof Creepie;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}
