package gg.moonflower.tolerablecreepers.common.entity.ai.sensing;

import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

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
        if (target.getType().is(TCTags.CREEPIE_AVOID))
            return false;
        if (creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY)
            return target instanceof Enemy && ((target instanceof Creepie && !this.isFriendlyCreepie(target)) || !(target instanceof Creeper));
        if (creepie.getCreepieType() == Creepie.CreepieType.NORMAL)
            return false;
        return target instanceof Player || this.isFriendlyCreepie(target);
    }

    private boolean isFriendlyCreepie(LivingEntity entity) {
        return entity instanceof Creepie creepie && creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}
