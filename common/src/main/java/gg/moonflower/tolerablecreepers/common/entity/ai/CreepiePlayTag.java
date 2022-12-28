package gg.moonflower.tolerablecreepers.common.entity.ai;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CreepiePlayTag extends Behavior<PathfinderMob> {

    private static final int MAX_FLEE_XZ_DIST = 20;
    private static final int MAX_FLEE_Y_DIST = 8;
    private static final float FLEE_SPEED_MODIFIER = 0.9F;
    private static final float CHASE_SPEED_MODIFIER = 0.9F;
    private static final int MAX_CHASERS_PER_TARGET = 5;
    private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

    public CreepiePlayTag() {
        super(ImmutableMap.of(
                MemoryModuleType.VISIBLE_VILLAGER_BABIES,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return serverLevel.getRandom().nextInt(AVERAGE_WAIT_TIME_BETWEEN_RUNS) == 0 && this.hasFriendsNearby(pathfinderMob);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob creepie, long l) {
        LivingEntity livingEntity = this.seeIfSomeoneIsChasingMe(creepie);
        if (livingEntity != null) {
            this.fleeFromChaser(creepie);
        } else {
            Optional<LivingEntity> optional = this.findSomeoneBeingChased(creepie);
            if (optional.isPresent()) {
                chaseKid(creepie, optional.get());
            } else {
                this.findSomeoneToChase(creepie).ifPresent(friend -> chaseKid(creepie, friend));
            }
        }
    }

    private void fleeFromChaser(PathfinderMob creepie) {
        for (int i = 0; i < 10; ++i) {
            Vec3 fleeSpot = LandRandomPos.getPos(creepie, MAX_FLEE_XZ_DIST, MAX_FLEE_Y_DIST);
            if (fleeSpot != null) {
                creepie.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(fleeSpot, FLEE_SPEED_MODIFIER, 0));
                return;
            }
        }
    }

    private static void chaseKid(PathfinderMob creepie, LivingEntity friend) {
        Brain<?> brain = creepie.getBrain();
        brain.setMemory(MemoryModuleType.INTERACTION_TARGET, friend);
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(friend, true));
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(friend, false), CHASE_SPEED_MODIFIER, 1));
    }

    private Optional<LivingEntity> findSomeoneToChase(PathfinderMob creepie) {
        return this.getFriendsNearby(creepie).stream().findAny();
    }

    private Optional<LivingEntity> findSomeoneBeingChased(PathfinderMob creepie) {
        Map<LivingEntity, Integer> map = this.checkHowManyChasersEachFriendHas(creepie);
        return map.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).filter(entry -> entry.getValue() > 0 && entry.getValue() <= MAX_CHASERS_PER_TARGET).map(Map.Entry::getKey).findFirst();
    }

    private Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(PathfinderMob creepie) {
        Map<LivingEntity, Integer> map = new HashMap<>();
        this.getFriendsNearby(creepie).stream().filter(this::isChasingSomeone).forEach(entity -> map.compute(this.whoAreYouChasing(entity), (__, integer) -> integer == null ? 1 : integer + 1));
        return map;
    }

    private List<LivingEntity> getFriendsNearby(PathfinderMob creepie) {
        return creepie.getBrain().getMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).orElse(Collections.emptyList());
    }

    private LivingEntity whoAreYouChasing(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
    }

    @Nullable
    private LivingEntity seeIfSomeoneIsChasingMe(PathfinderMob creepie) {
        return this.getFriendsNearby(creepie).stream().filter(friend -> this.isFriendChasingMe(creepie, friend)).findAny().orElse(null);
    }

    private boolean isChasingSomeone(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
    }

    private boolean isFriendChasingMe(PathfinderMob creepie, LivingEntity friend) {
        return creepie.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter(entity -> entity == friend).isPresent();
    }

    private boolean hasFriendsNearby(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
    }
}
