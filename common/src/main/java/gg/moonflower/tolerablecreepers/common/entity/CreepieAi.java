package gg.moonflower.tolerablecreepers.common.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimatedEntity;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationState;
import gg.moonflower.tolerablecreepers.common.entity.ai.*;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new RunIf<>(Creepie::canMove, new MoveToTargetSink(), true),
                new RunIf<>(creepie -> creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY, new CreepieFollowOwner(16, 1.0F), false),
                new StopBeingAngryIfTargetDead<>()
        ));
    }

    private static void initIdleActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
                new SetEntityLookTarget(8.0F),
                new StartAttacking<>(CreepieAi::findNearestValidAttackTarget),
                new CreepieDance(),
                avoidRepellent(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors()
        ));
    }

    private static void initFightActivity(Creepie creepie, Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(new StopAttackingIfTargetInvalid<>(livingEntity -> !isNearestValidAttackTarget(creepie, livingEntity)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new CreepieAttack()), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.CELEBRATE, ImmutableList.of(
                Pair.of(5, avoidRepellent()),
                Pair.of(6, new StartAttacking<>(CreepieAi::findNearestValidAttackTarget)),
                Pair.of(7, new CreepieDance()),
                Pair.of(8, new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(TCEntities.CREEPIE.get(), 8.0F), 1), Pair.of(new DoNothing(10, 20), 1))))
        ), ImmutableSet.of(Pair.of(MemoryModuleType.DANCING, MemoryStatus.VALUE_PRESENT)), ImmutableSet.of(MemoryModuleType.DANCING));
    }

    private static void initRetreatActivity(Brain<Creepie> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(avoidRepellent(), SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors()), MemoryModuleType.AVOID_TARGET);
    }

    private static void initPlayActivity(Brain<Creepie> brain) {
        brain.addActivity(Activity.PLAY, ImmutableList.of(
                Pair.of(99, new StartAttacking<>(CreepieAi::findNearestValidAttackTarget)),
                Pair.of(0, new MoveToTargetSink(80, 120)),
                Pair.of(5, createIdleLookBehaviors()),
                Pair.of(5, new CreepieDance()),
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

    private static boolean isFriendlyCreepieTarget(Entity entity) {
        return entity instanceof Creepie creepie && creepie.getCreepieType() != Creepie.CreepieType.FRIENDLY;
    }

    private static void updateTarget(LivingEntity owner, Creepie creepie) {
        LivingEntity lastHurt = owner.getLastHurtMob();
        LivingEntity lastHurtBy = owner.getLastHurtByMob();
        if (lastHurt == null && lastHurtBy == null) {
            Level level = creepie.getLevel();
            Creepie closest = level.getNearestEntity(level.getEntitiesOfClass(Creepie.class, owner.getBoundingBox().inflate(6, 2, 6), CreepieAi::isFriendlyCreepieTarget), TargetingConditions.DEFAULT, owner, owner.getX(), owner.getY(), owner.getZ());
            if (closest != null) {
                creepie.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, closest);
            }

            return;
        }

        if (lastHurt == null) {
            creepie.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, Optional.of(lastHurtBy));
            return;
        }

        if (lastHurtBy == null) {
            creepie.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, Optional.of(lastHurt));
            return;
        }

        long lastHurtTime = owner.getLastHurtMobTimestamp();
        long lastHurtByTime = owner.getLastHurtByMobTimestamp();
        creepie.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, Optional.of(lastHurtByTime >= lastHurtTime ? lastHurtBy : lastHurt));
    }

    protected static void updateActivity(Creepie creepie) {
        Brain<Creepie> brain = creepie.getBrain();

        if (creepie.getCreepieType() == Creepie.CreepieType.FRIENDLY) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.CELEBRATE, Activity.PLAY, Activity.IDLE));
            if (creepie.getOwner() instanceof LivingEntity livingOwner) {
                updateTarget(livingOwner, creepie);
                if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                    brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
                }
            }
        } else {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.IDLE));
        }

        creepie.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));

        BlockPos celebration = creepie.getCreepieType() != Creepie.CreepieType.ENRAGED ? brain.getMemory(MemoryModuleType.CELEBRATE_LOCATION).orElse(null) : null;
        if (celebration == null || !celebration.closerToCenterThan(creepie.position(), Creepie.PARTY_DISTANCE) || !creepie.level.getBlockState(celebration).is(TCTags.CREEPIE_PARTY_SPOTS)) {
            brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        }

        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        } else {
            if (celebration != null && creepie.level.getBlockState(celebration).is(Blocks.JUKEBOX)) {
                brain.setMemory(MemoryModuleType.DANCING, true);
            }
        }

        if (!creepie.isSad()) {
            if (brain.hasMemoryValue(MemoryModuleType.DANCING)) {
                creepie.getNavigation().stop();
                if (!creepie.isDancing() && !creepie.isAnimationTransitioning()) {
                    AnimatedEntity.setAnimation(creepie, Creepie.DANCE, 5);
                }
            } else if (creepie.isDancing() && !creepie.isAnimationTransitioning()) {
                AnimatedEntity.setAnimation(creepie, AnimationState.EMPTY, 2);
            }
        } else {
            brain.eraseMemory(MemoryModuleType.DANCING);
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
