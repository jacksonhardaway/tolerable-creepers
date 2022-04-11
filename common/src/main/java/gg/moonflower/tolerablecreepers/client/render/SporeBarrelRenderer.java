package gg.moonflower.tolerablecreepers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import gg.moonflower.tolerablecreepers.common.entity.PrimedSporeBarrel;
import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class SporeBarrelRenderer extends EntityRenderer<PrimedSporeBarrel> {

    public SporeBarrelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(PrimedSporeBarrel primedTnt, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        int j = primedTnt.getFuse();
        if ((float) j - g + 1.0F < 10.0F) {
            float h = 1.0F - ((float) j - g + 1.0F) / 10.0F;
            h = Mth.clamp(h, 0.0F, 1.0F);
            h *= h;
            h *= h;
            float k = 1.0F + h * 0.3F;
            poseStack.scale(k, k, k);
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5, -0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(TCBlocks.SPORE_BARREL.get().defaultBlockState(), poseStack, multiBufferSource, i, j / 5 % 2 == 0);
        poseStack.popPose();
        super.render(primedTnt, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedSporeBarrel entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
