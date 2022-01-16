package gg.moonflower.tolerablecreepers.core.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Zoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zoglin.class)
public class ZoglinMixin {
    @Inject(method = "isTargetable", at = @At("HEAD"))
    private void canAttackType(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        EntityType<?> entityType = livingEntity.getType();
        if (entityType != EntityType.ZOGLIN && Sensor.isEntityAttackable((Zoglin) (Object) this, livingEntity))
            cir.setReturnValue(true);
    }
}
