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
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class CreepieSpecificSensor extends Sensor<Creepie> {

    private static final Set<MemoryModuleType<?>> REQUIRES = ImmutableSet.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_REPELLENT,
            TCEntities.HIDING_SPOT.get(),
            MemoryModuleType.AVOID_TARGET
    );

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return REQUIRES;
    }

    @Override
    protected void doTick(ServerLevel level, Creepie creepie) {
        Brain<?> brain = creepie.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearest(creepie, pos -> level.getBlockState(pos).is(TCTags.CREEPIE_REPELLENTS)));
        // Only try to hide if there is no other entity inside the block
        brain.setMemory(TCEntities.HIDING_SPOT.get(), findNearest(creepie, pos -> level.getBlockState(pos).is(TCTags.CREEPIE_HIDING_SPOT) && level.getEntities(creepie, new AABB(pos).inflate(0.5, 0.0, 0.5)).isEmpty()));
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        brain.setMemory(MemoryModuleType.AVOID_TARGET, nearestVisibleLivingEntities.findClosest(e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && (!e.isSteppingCarefully() || creepie.distanceToSqr(e) <= 64.0) && (creepie.getCreepieType() == Creepie.CreepieType.NORMAL || e.getType().is(TCTags.CREEPIE_AVOID))));
    }

    private static Optional<BlockPos> findNearest(LivingEntity entity, Predicate<BlockPos> predicate) {
        return BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, predicate);
    }
}
