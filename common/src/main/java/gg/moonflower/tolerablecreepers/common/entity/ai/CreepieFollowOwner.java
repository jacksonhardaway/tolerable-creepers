package gg.moonflower.tolerablecreepers.common.entity.ai;

import com.google.common.collect.ImmutableMap;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Objects;

public class CreepieFollowOwner extends Behavior<Creepie> {

    private final int closeEnoughDist;
    private final float speedModifier;

    public CreepieFollowOwner(int closeEnoughDist, float speedModifier) {
        super(
                ImmutableMap.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.WALK_TARGET,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryStatus.REGISTERED
                )
        );
        this.closeEnoughDist = closeEnoughDist;
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Creepie creepie) {
        if (creepie.getCreepieType() != Creepie.CreepieType.FRIENDLY) {
            return false;
        }
        Entity owner = creepie.getOwner();
        return owner != null && creepie.getSensing().hasLineOfSight(owner);
    }

    @Override
    protected void start(ServerLevel level, Creepie creepie, long time) {
        Entity owner = Objects.requireNonNull(creepie.getOwner());
        boolean bl = owner.closerThan(creepie, this.closeEnoughDist);
        if (!bl) {
            BehaviorUtils.setWalkAndLookTargetMemories(creepie, owner, this.speedModifier, this.closeEnoughDist);
        }
    }
}

