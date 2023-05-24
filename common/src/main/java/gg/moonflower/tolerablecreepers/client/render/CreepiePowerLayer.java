package gg.moonflower.tolerablecreepers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.pinwheel.api.geometry.GeometryRenderer;
import gg.moonflower.pollen.api.render.animation.v1.entity.AnimatedGeometryEntityModel;
import gg.moonflower.pollen.api.render.geometry.v1.GeometryBufferSource;
import gg.moonflower.pollen.api.render.geometry.v1.MinecraftGeometryRenderer;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class CreepiePowerLayer extends RenderLayer<Creepie, AnimatedGeometryEntityModel<Creepie>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TolerableCreepers.MOD_ID, "textures/entity/creepie/creepie_armor.png");
    private final CreepieModelWrapper model;

    public CreepiePowerLayer(RenderLayerParent<Creepie, AnimatedGeometryEntityModel<Creepie>> renderLayerParent) {
        super(renderLayerParent);
        this.model = new CreepieModelWrapper();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Creepie creepie, float f, float g, float partialTicks, float j, float k, float l) {
        if (creepie.isPowered()) {
            float offset = (float) creepie.tickCount + partialTicks;
            this.model.setAnimations(this.getParentModel().getAnimationNames());
            this.model.prepareMobModel(creepie, f, g, partialTicks);
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.setupAnim(creepie, f, g, j, k, l);
            this.model.setBuffer(Geome);
            this.wrapper.setParent(this.model.getModel());
            MinecraftGeometryRenderer.getInstance().render(this.wrapper, TEXTURE, buffer, matrixStack, packedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F, builder -> TCRenderTypes.applyCreepieArmor(builder, TEXTURE, offset * 0.02F % 1.0F, offset * 0.02F % 1.0F));
        }
    }
}
