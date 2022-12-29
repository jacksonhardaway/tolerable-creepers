package gg.moonflower.tolerablecreepers.core.mixin.client;

import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperModel.class)
public class CreeperModelMixin<T extends Entity> {

    @Shadow
    @Final
    private ModelPart head;

    @Unique
    private PartPose headPose;

    @Inject(method = "setupAnim", at = @At("HEAD"))
    public void setupAnim(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (this.headPose == null) {
            this.headPose = this.head.storePose();
        }

        boolean dancing = entity.getLevel().getEntities(entity, entity.getBoundingBox().inflate(5.0F)).stream().anyMatch(e -> e instanceof Creepie creepie && creepie.distanceToSqr(entity) <= 25.0F && creepie.isDancing());
        if (dancing) {
            float n = h / 60.0F;
            this.head.x = this.headPose.x + Mth.sin(n * 10.0F) * 0.25F;
            this.head.y = this.headPose.y + Mth.sin(n * 40.0F) + 1.0F;
        } else {
            this.head.loadPose(this.headPose);
        }
    }
}
