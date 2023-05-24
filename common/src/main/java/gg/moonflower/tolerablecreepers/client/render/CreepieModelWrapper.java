package gg.moonflower.tolerablecreepers.client.render;

import gg.moonflower.pollen.api.render.animation.v1.entity.AnimatedGeometryEntityModel;
import gg.moonflower.pollen.api.render.geometry.v1.GeometryBufferSource;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.resources.ResourceLocation;

public class CreepieModelWrapper extends AnimatedGeometryEntityModel<Creepie> {

    private GeometryBufferSource buffer;

    public CreepieModelWrapper() {
        super(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_armor"));
    }

    public void setBuffer(GeometryBufferSource buffer) {
        this.buffer = buffer;
    }

    @Override
    public GeometryBufferSource getGeometryBuffers() {
        return buffer;
    }
}
