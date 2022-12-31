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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class CreepieSpecificSensor extends Sensor<Creepie> {

    private static final Set<MemoryModuleType<?>> REQUIRES = ImmutableSet.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_REPELLENT,
            TCEntities.HIDING_SPOT.get(),
            MemoryModuleType.CELEBRATE_LOCATION,
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
        brain.setMemory(TCEntities.HIDING_SPOT.get(), findNearest(creepie, pos -> level.getBlockState(pos).is(TCTags.CREEPIE_HIDING_SPOTS) && level.getEntities(creepie, new AABB(pos).inflate(0.5, 0.0, 0.5)).isEmpty()));
        brain.setMemory(MemoryModuleType.CELEBRATE_LOCATION, findNearestCelebration(level, creepie));

        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        brain.setMemory(MemoryModuleType.AVOID_TARGET, nearestVisibleLivingEntities.findClosest(e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && e.getType().is(TCTags.CREEPIE_AVOID) && (!e.isSteppingCarefully() || creepie.distanceToSqr(e) <= 64.0)));
    }

    private static Optional<BlockPos> findNearest(LivingEntity entity, Predicate<BlockPos> predicate) {
        return BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, predicate);
    }

    private static Optional<BlockPos> findNearestCelebration(ServerLevel level, LivingEntity entity) {
        Optional<BlockPos> jukeboxPos = BlockPos.findClosestMatch(entity.blockPosition(), 4, 4, pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.is(TCTags.CREEPIE_PARTY_SPOTS)) {
                return false;
            }
            return state.is(Blocks.JUKEBOX) && state.getValue(JukeboxBlock.HAS_RECORD);
        });
        if (jukeboxPos.isPresent()) {
            return jukeboxPos;
        }

        return BlockPos.findClosestMatch(entity.blockPosition(), (int) Creepie.PARTY_DISTANCE, (int) Creepie.PARTY_DISTANCE, pos -> {
            BlockState state = level.getBlockState(pos);
            return state.is(TCTags.CREEPIE_PARTY_SPOTS) && !state.is(Blocks.JUKEBOX);
        });
    }
}
