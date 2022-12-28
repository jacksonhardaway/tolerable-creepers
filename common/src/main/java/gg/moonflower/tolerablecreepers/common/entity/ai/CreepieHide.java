package gg.moonflower.tolerablecreepers.common.entity.ai;

import com.google.common.collect.ImmutableMap;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class CreepieHide extends Behavior<Creepie> {

    private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

    public CreepieHide() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                TCEntities.HIDING_SPOT.get(), MemoryStatus.VALUE_PRESENT,
                TCEntities.HAS_FRIENDS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Creepie creepie) {
        return !creepie.isHiding() && serverLevel.getRandom().nextInt(AVERAGE_WAIT_TIME_BETWEEN_RUNS) == 0 && creepie.getBrain().getMemory(TCEntities.HAS_FRIENDS.get()).orElse(false);
    }

    @Override
    protected void start(ServerLevel serverLevel, Creepie creepie, long l) {
        Brain<?> brain = creepie.getBrain();
        brain.setMemory(MemoryModuleType.WALK_TARGET, brain.getMemory(TCEntities.HIDING_SPOT.get()).map(pos -> new WalkTarget(pos.above(), 0.6F, 1)));
    }
}
