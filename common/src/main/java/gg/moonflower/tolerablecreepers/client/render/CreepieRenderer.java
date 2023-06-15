package gg.moonflower.tolerablecreepers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.pollen.api.render.animation.v1.entity.GeometryEntityRenderer;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CreepieRenderer extends GeometryEntityRenderer<Creepie> {

    private static final ResourceLocation MODEL = new ResourceLocation(TolerableCreepers.MOD_ID, "creepie");

    public CreepieRenderer(EntityRendererProvider.Context context) {
        super(context, MODEL, 0.25F);
        this.addLayer(new CreepiePowerLayer(this));
    }

    @Override
    protected float getBob(Creepie entity, float partialTicks) {
        return (float)entity.tickCount + partialTicks; // entity.getRenderAnimationTick(partialTicks)
    }

    @Override
    protected void scale(Creepie creeper, PoseStack poseStack, float f) {
        float g = creeper.getSwelling(f);
        float h = 1.0F + Mth.sin(g * 100.0F) * g * 0.01F;
        g = Mth.clamp(g, 0.0F, 1.0F);
        g *= g;
        g *= g;
        float i = (1.0F + g * 0.4F) * h;
        float j = (1.0F + g * 0.1F) / h;
        poseStack.scale(i, j, i);
    }

    @Override
    protected float getWhiteOverlayProgress(Creepie creeper, float f) {
        float g = creeper.getSwelling(f);
        return (int) (g * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(g, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureTableLocation(Creepie creepie, float v) {
        return creepie.getCreepieType().getTexture();
    }
}
