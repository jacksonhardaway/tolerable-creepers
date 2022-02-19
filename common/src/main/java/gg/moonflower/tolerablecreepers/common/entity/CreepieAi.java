package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import gg.moonflower.tolerablecreepers.common.entity.ai.CreepieAttack;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CreepieAi {

    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);

    protected static Brain<?> makeBrain(Creepie creepie, Brain<Creepie> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(creepie, brain);
        initCelebrateActivity(brain);
        initRetreatActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(Creepie creepie) {
//        int i = TIME_BETWEEN_HUNTS.sample(piglin.level.random);
//        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, i);
    }

    private static void initCoreActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new StopBeingAngryIfTargetDead<>()
        ));
    }

    private static void initIdleActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
                new SetEntityLookTarget(8.0F),
                new StartAttacking<>(CreepieAi::findNearestValidAttackTarget),
                avoidRepellent(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                new SetLookAndInteract(EntityType.PLAYER, 4)
        ));
    }

    private static void initFightActivity(Creepie creepie, Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(new StopAttackingIfTargetInvalid<>(livingEntity -> !isNearestValidAttackTarget(creepie, livingEntity)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new CreepieAttack()), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 5, ImmutableList.of(avoidRepellent(), new StartAttacking<>(CreepieAi::findNearestValidAttackTarget), new RunIf<>(creepie -> creepie.getAnimationState() != Creepie.DANCE, new GoToCelebrateLocation<>(2, 1.0F)), new RunIf<>(Creepie::isDancing, new GoToCelebrateLocation<>(4, 0.6F)), new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(TCEntities.CREEPIE.get(), 8.0F), 1), Pair.of(new DoNothing(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initRetreatActivity(Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(avoidRepellent(), SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors()), MemoryModuleType.AVOID_TARGET);
    }

    private static RunOne<Creepie> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 1), Pair.of(new SetEntityLookTarget(8.0F), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Creepie> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(0.6F), 2), Pair.of(new SetWalkTargetFromLookTarget(0.6F, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static SetWalkTargetAwayFrom<BlockPos> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
    }

    protected static void updateActivity(Creepie creepie) {
        Brain<Creepie> brain = creepie.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.CELEBRATE, Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);

//        if (activity != activity2) {
//            getSoundForCurrentActivity(creepie).ifPresent(creepie::playSound);
//        }

        creepie.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));

        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        }

        if (brain.hasMemoryValue(MemoryModuleType.DANCING)) {
            creepie.setAnimationState(Creepie.DANCE);
        } else if (creepie.isDancing()) {
            creepie.resetAnimationState();
        }
    }

    protected static void wasHurtBy(Creepie creepie, LivingEntity attacker) {
        if (!(attacker instanceof Creepie creepieAttacker) || creepie.getCreepieType() != creepieAttacker.getCreepieType()) {
            Brain<Creepie> brain = creepie.getBrain();
            brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            brain.eraseMemory(MemoryModuleType.DANCING);

            getAvoidTarget(creepie).ifPresent(avoidTarget -> {
                if (avoidTarget.getType() != attacker.getType())
                    brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
            });

            if (Sensor.isEntityAttackableIgnoringLineOfSight(creepie, attacker)) {
                creepie.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                creepie.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);
            }
        }
    }

    private static boolean isNearestValidAttackTarget(Creepie creepie, LivingEntity target) {
        return findNearestValidAttackTarget(creepie).filter(e -> e == target).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Creepie creepie) {
        return creepie.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Creepie piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map(activity -> getSoundForActivity(piglin, activity));
    }

    private static SoundEvent getSoundForActivity(Creepie piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        } else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        } else if (activity == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        } else if (activity == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        } else {
            return isNearRepellent(piglin) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
        }
    }

    private static boolean isNearAvoidTarget(Creepie creepie) {
        Brain<Creepie> brain = creepie.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) && brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(creepie, 12.0);
    }

    private static void stopWalking(Creepie piglin) {
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static Optional<LivingEntity> getAngerTarget(Creepie creepie) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(creepie, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(Creepie creepie) {
        return creepie.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? creepie.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(Creepie abstractCreepie) {
        return abstractCreepie.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? abstractCreepie.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    private static Vec3 getRandomNearbyPos(Creepie creepie) {
        Vec3 vec3 = LandRandomPos.getPos(creepie, 4, 2);
        return vec3 == null ? creepie.position() : vec3;
    }

    protected static boolean isIdle(Creepie creepie) {
        return creepie.getBrain().isActive(Activity.IDLE);
    }

    private static boolean isNearRepellent(Creepie creepie) {
        return creepie.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean wasHurtRecently(LivingEntity entity) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }
}
