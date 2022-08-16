package gg.moonflower.tolerablecreepers.common.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class CreepieFriendSensor extends Sensor<Creepie> {

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
    protected void doTick(ServerLevel serverLevel, Creepie entity) {
        Brain<?> brain = entity.getBrain();
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        brain.setMemory(TCEntities.NEARBY_FRIEND_MEMORY.get(), nearestVisibleLivingEntities.findClosest(e -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && entity.distanceToSqr(e) <= 64.0 && e.getType().is(TCTags.CREEPIE_FRIEND)));
    }

    private boolean isFriend(Creepie entity, LivingEntity other) {
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(other))
            return false;
        if (!entity.isFriend(other))
            return false;
        return !(entity.distanceToSqr(other) >= Creepie.CREEPER_DISTANCE * Creepie.CREEPER_DISTANCE);
    }
}
