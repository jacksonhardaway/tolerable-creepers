package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.resource.v1.TagRegistry;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class TCTags {

    public static TagKey<EntityType<?>> EXPLOSION_IMMUNE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_immune"));
    public static TagKey<EntityType<?>> EXPLOSION_PRONE = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "explosion_prone"));

    public static TagKey<EntityType<?>> CREEPIE_AVOID = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_avoid"));
    public static TagKey<EntityType<?>> CREEPIE_FRIENDS = TagRegistry.bindEntityType(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_friends"));

    public static TagKey<Block> CREEPIE_REPELLENTS = TagRegistry.bindBlock(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_repellents"));
    public static TagKey<Block> CREEPIE_HIDING_SPOTS = TagRegistry.bindBlock(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_hiding_spots"));
    public static TagKey<Block> CREEPIE_PARTY_SPOTS = TagRegistry.bindBlock(new ResourceLocation(TolerableCreepers.MOD_ID, "creepie_party_spots"));
    public static TagKey<Block> FIRE_BOMB_EXPLODE = TagRegistry.bindBlock(new ResourceLocation(TolerableCreepers.MOD_ID, "fire_bomb_explode"));
}
