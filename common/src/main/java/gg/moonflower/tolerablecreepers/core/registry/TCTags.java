package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.resource.TagRegistry;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class TCTags {

    public static Tag.Named<EntityType<?>> EXPLOSION_IMMUNE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_immune"));
    public static Tag.Named<EntityType<?>> EXPLOSION_PRONE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_prone"));

    public static Tag.Named<EntityType<?>> CREEPIE_AVOID = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_avoid"));

    public static Tag.Named<Block> CREEPIE_REPELLENTS = TagRegistry.bindBlock(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_repellents"));
}
