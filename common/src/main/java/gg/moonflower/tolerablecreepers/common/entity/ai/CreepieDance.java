package gg.moonflower.tolerablecreepers.common.entity.ai;

import com.google.common.collect.ImmutableMap;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;

public class CreepieDance extends Behavior<Creepie> {

    private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 50;
    private static final int STOP_CHANCE = 100;

    public CreepieDance() {
        super(ImmutableMap.of(
                MemoryModuleType.CELEBRATE_LOCATION, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.DANCING, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Creepie creepie) {
        Brain<?> brain = creepie.getBrain();
        if (serverLevel.getBlockState(brain.getMemory(MemoryModuleType.CELEBRATE_LOCATION).get()).is(TCTags.CREEPIE_FORCE_PARTY_SPOTS)) {
            return false;
        }
        return serverLevel.getRandom().nextInt(brain.getMemory(MemoryModuleType.DANCING).orElse(false) ? STOP_CHANCE : AVERAGE_WAIT_TIME_BETWEEN_RUNS) == 0;
    }

    @Override
    protected void start(ServerLevel serverLevel, Creepie creepie, long l) {
        Brain<?> brain = creepie.getBrain();
        if (brain.getMemory(MemoryModuleType.DANCING).orElse(false)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
            return;
        }
        brain.setMemory(MemoryModuleType.DANCING, true);
    }
}
