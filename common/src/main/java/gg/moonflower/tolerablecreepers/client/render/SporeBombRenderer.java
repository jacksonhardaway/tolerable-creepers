package gg.moonflower.tolerablecreepers.client.render;

import gg.moonflower.tolerablecreepers.common.entity.SporeBomb;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class SporeBombRenderer extends ThrowableBombRenderer<SporeBomb> {

    private static final ResourceLocation MODEL = new ResourceLocation(TolerableCreepers.MOD_ID, "entity/spore_bomb");

    public SporeBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getModelLocation(SporeBomb entity) {
        return MODEL;
    }
}
