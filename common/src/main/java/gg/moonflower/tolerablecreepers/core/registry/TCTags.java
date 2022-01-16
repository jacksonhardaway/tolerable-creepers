package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.resource.TagRegistry;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

public class TCTags {
    public static Tag.Named<EntityType<?>> EXPLOSION_IMMUNE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_immune"));
    public static Tag.Named<EntityType<?>> EXPLOSION_PRONE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_prone"));
}
