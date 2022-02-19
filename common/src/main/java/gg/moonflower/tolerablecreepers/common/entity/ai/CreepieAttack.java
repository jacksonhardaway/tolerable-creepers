package gg.moonflower.tolerablecreepers.common.entity.ai;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Creeper;

public class CreepieAttack extends Behavior<Creeper> {

    public CreepieAttack() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Creeper creeper) {
        LivingEntity livingEntity = creeper.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return creeper.getSwellDir() > 0 || livingEntity != null && creeper.distanceToSqr(livingEntity) < 4.0;
    }

    @Override
    protected void stop(ServerLevel serverLevel, Creeper creeper, long l) {
        creeper.getNavigation().setSpeedModifier(1.0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Creeper creeper, long l) {
        return this.checkExtraStartConditions(level, creeper);
    }

    @Override
    protected void tick(ServerLevel level, Creeper creeper, long l) {
        LivingEntity target = creeper.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            creeper.setSwellDir(-1);
            creeper.getNavigation().setSpeedModifier(1.4);
        } else if (creeper.distanceToSqr(target) > 25.0) {
            creeper.setSwellDir(-1);
            creeper.getNavigation().setSpeedModifier(1.4);
        } else if (!creeper.getSensing().hasLineOfSight(target)) {
            creeper.setSwellDir(-1);
            creeper.getNavigation().setSpeedModifier(1.4);
        } else {
            creeper.setSwellDir(1);
            creeper.getNavigation().setSpeedModifier(0.5);
        }
    }
}
