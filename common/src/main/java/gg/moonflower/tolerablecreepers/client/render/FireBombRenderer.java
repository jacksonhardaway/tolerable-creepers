package gg.moonflower.tolerablecreepers.client.render;

import gg.moonflower.tolerablecreepers.common.entity.FireBomb;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireBombRenderer extends ThrowableBombRenderer<FireBomb> {

    private static final ResourceLocation MODEL = new ResourceLocation(TolerableCreepers.MOD_ID, "entity/fire_bomb");

    public FireBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getModelLocation(FireBomb entity) {
        return MODEL;
    }
}
