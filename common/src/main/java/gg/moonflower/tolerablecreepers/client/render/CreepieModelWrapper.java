package gg.moonflower.tolerablecreepers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import gg.moonflower.pollen.pinwheel.api.client.animation.AnimatedModel;
import gg.moonflower.pollen.pinwheel.api.client.geometry.GeometryModel;
import gg.moonflower.pollen.pinwheel.api.client.texture.GeometryAtlasTexture;
import gg.moonflower.pollen.pinwheel.api.common.animation.AnimationData;
import gg.moonflower.pollen.pinwheel.api.common.geometry.GeometryModelData;
import gg.moonflower.pollen.pinwheel.api.common.texture.GeometryModelTexture;
import io.github.ocelot.molangcompiler.api.MolangRuntime;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class CreepieModelWrapper implements GeometryModel, AnimatedModel {

    private GeometryModel parent;

    @Override
    public void applyAnimations(float animationTime, MolangRuntime.Builder runtime, AnimationData... animations) {
        if (this.parent instanceof AnimatedModel)
            ((AnimatedModel) this.parent).applyAnimations(animationTime, runtime, animations);
    }

    @Override
    public GeometryModelData.Locator[] getLocators(String part) {
        return this.parent instanceof AnimatedModel ? ((AnimatedModel) this.parent).getLocators(part) : new GeometryModelData.Locator[0];
    }

    @Override
    public void render(String material, GeometryModelTexture texture, PoseStack matrixStack, VertexConsumer builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.parent.render(material, texture, matrixStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void resetTransformation() {
        this.parent.resetTransformation();
    }

    @Override
    public void copyAngles(@Nullable String parent, ModelPart modelPart) {
        this.parent.copyAngles(parent, modelPart);
    }

    @Override
    public Optional<ModelPart> getModelPart(String part) {
        return this.parent.getModelPart(part);
    }

    @Override
    public ModelPart[] getChildRenderers(String part) {
        return this.parent.getChildRenderers(part);
    }

    @Override
    public ModelPart[] getModelParts() {
        return this.parent.getModelParts();
    }

    @Override
    public String[] getParentModelKeys() {
        return this.parent.getParentModelKeys();
    }

    @Override
    public String[] getMaterialKeys() {
        return this.parent.getMaterialKeys();
    }

    @Override
    public float getTextureWidth() {
        return this.parent.getTextureWidth();
    }

    @Override
    public float getTextureHeight() {
        return this.parent.getTextureHeight();
    }

    @Override
    public VertexConsumer getBuffer(MultiBufferSource buffer, GeometryAtlasTexture atlas, GeometryModelTexture texture, @Nullable Consumer<RenderType.CompositeState.CompositeStateBuilder> renderTypeConsumer) {
        return buffer.getBuffer(texture.getLayer().getRenderType(texture, atlas, renderTypeConsumer));
    }

    public void setParent(GeometryModel parent) {
        this.parent = parent;
    }
}
