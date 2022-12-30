package gg.moonflower.tolerablecreepers.core.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin extends AbstractGolem implements NeutralMob {
    @Shadow public abstract boolean isPlayerCreated();

    protected IronGolemMixin(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doPush", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/AbstractGolem;doPush(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.BEFORE))
    private void doPush(Entity entity, CallbackInfo ci) {
        if (entity instanceof Creeper && this.getRandom().nextInt(20) == 0)
            this.setTarget((LivingEntity) entity);
    }

    @Inject(method = "canAttackType", at = @At(value = "RETURN", ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
    private void canAttackType(EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (!(this.isPlayerCreated() && entityType == EntityType.PLAYER))
            cir.setReturnValue(super.canAttackType(entityType));
    }
}
