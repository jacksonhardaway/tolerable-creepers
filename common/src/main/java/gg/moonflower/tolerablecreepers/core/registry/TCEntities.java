package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class TCEntities {
    public static final PollinatedRegistry<EntityType<?>> ENTITIES = PollinatedRegistry.create(Registry.ENTITY_TYPE, TolerableCreepers.MOD_ID);

    public static final Supplier<EntityType<CreeperSpores>> CREEPER_SPORES = ENTITIES.register("creeper_spores", () -> EntityType.Builder.<CreeperSpores>of(CreeperSpores::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).build("creeper_spores"));
}
