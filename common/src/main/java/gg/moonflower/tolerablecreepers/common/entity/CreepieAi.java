package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.tolerablecreepers.common.entity.ai.CreepieAttack;
import gg.moonflower.tolerablecreepers.common.entity.ai.CreepieHide;
import gg.moonflower.tolerablecreepers.common.entity.ai.CreepiePlayTag;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

public class CreepieAi {

    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);

    protected static Brain<?> makeBrain(Creepie creepie, Brain<Creepie> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(creepie, brain);
        initCelebrateActivity(brain);
        initRetreatActivity(brain);
        initPlayActivity(brain);
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
                new RunIf<>(Creepie::canMove, new MoveToTargetSink(), true),
                new StopBeingAngryIfTargetDead<>()
        ));
    }

    private static void initIdleActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
                new SetEntityLookTarget(8.0F),
                new StartAttacking<>(CreepieAi::findNearestValidAttackTarget),
                avoidRepellent(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors()
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

    private static void initPlayActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.PLAY, ImmutableList.of(
                Pair.of(99, new StartAttacking<>(CreepieAi::findNearestValidAttackTarget)),
                Pair.of(0, new MoveToTargetSink(80, 120)),
                Pair.of(5, createIdleLookBehaviors()),
                Pair.of(5, new CreepiePlayTag()),
                Pair.of(8, new CreepieHide()),
                Pair.of(
                        5,
                        new RunOne<>(
                                ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT),
                                ImmutableList.of(
                                        Pair.of(InteractWith.of(EntityType.CREEPER, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 2),
                                        Pair.of(InteractWith.of(TCEntities.CREEPIE.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1),
                                        Pair.of(createIdleMovementBehaviors(), 1),
                                        Pair.of(new SetWalkTargetFromLookTarget(0.5F, 2), 1),
                                        Pair.of(new CreepieHide(), 2),
                                        Pair.of(new DoNothing(20, 40), 2)
                                )
                        )
                )
        ));
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

        if (creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.CELEBRATE, Activity.PLAY, Activity.IDLE));
        } else {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.IDLE));
        }

        creepie.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));

        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        }

        if (brain.hasMemoryValue(MemoryModuleType.DANCING)) {
            if (!creepie.isDancing()) {
                AnimatedEntity.setAnimation(creepie, Creepie.DANCE);
            }
        } else if (creepie.isDancing()) {
            creepie.resetAnimationState();
            AnimatedEntity.setAnimation(creepie, creepie.getAnimationState(), creepie.getAnimationTransitionLength());
        }

        boolean shouldBeSad = creepie.getCreepieType() != Creepie.CreepieType.ENRAGED && brain.getMemory(TCEntities.HAS_FRIENDS.get()).orElse(false);
        if (creepie.isSad() == shouldBeSad) {
            creepie.setSad(!shouldBeSad);
        }
    }

    protected static void wasHurtBy(Creepie creepie, LivingEntity attacker) {
        if (!(attacker instanceof Creepie creepieAttacker) || creepie.getCreepieType() != creepieAttacker.getCreepieType()) {
            Brain<Creepie> brain = creepie.getBrain();
            brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            brain.eraseMemory(MemoryModuleType.DANCING);
            brain.eraseMemory(TCEntities.HIDING_SPOT.get());

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

    public static Optional<LivingEntity> getAvoidTarget(Creepie creepie) {
        return creepie.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? creepie.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }
}
