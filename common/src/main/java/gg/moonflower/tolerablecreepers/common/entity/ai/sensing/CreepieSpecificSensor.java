package gg.moonflower.tolerablecreepers.common.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Optional;
import java.util.Set;

public class CreepieSpecificSensor extends Sensor<Creepie> {

    private static final Set<MemoryModuleType<?>> REQUIRES = ImmutableSet.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.NEAREST_REPELLENT
    );

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return REQUIRES;
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Creepie creepie) {
        Brain<?> brain = creepie.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(serverLevel, creepie));
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        brain.setMemory(MemoryModuleType.AVOID_TARGET, nearestVisibleLivingEntities.findClosest(e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && (!e.isSteppingCarefully() || creepie.distanceToSqr(e) <= 64.0) && (creepie.getCreepieType() == Creepie.CreepieType.NORMAL || e.getType().is(TCTags.CREEPIE_AVOID))));
        brain.setMemory(TCEntities.NEARBY_FRIEND_MEMORY.get(), nearestVisibleLivingEntities.findClosest(e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && creepie.distanceToSqr(e) <= 64.0 && e.getType().is(TCTags.CREEPIE_FRIEND)));
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel level, LivingEntity entity) {
        return BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, blockPos -> isValidRepellent(level, blockPos));
    }

    private static boolean isValidRepellent(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(TCTags.CREEPIE_REPELLENTS);
    }
}
