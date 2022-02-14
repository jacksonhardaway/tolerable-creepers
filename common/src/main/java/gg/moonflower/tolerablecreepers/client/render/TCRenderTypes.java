package gg.moonflower.tolerablecreepers.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public final class TCRenderTypes extends RenderType {

    private TCRenderTypes(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static void applyCreepieArmor(CompositeState.CompositeStateBuilder builder, ResourceLocation texture, float offsetX, float offsetY) {
        builder.setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new TextureStateShard(texture, false, false)).setTexturingState(new OffsetTexturingStateShard(offsetX, offsetY)).setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL);
    }
}
