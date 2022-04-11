package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.common.entity.PrimedSporeBarrel;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class TCEntities {

    public static final PollinatedRegistry<EntityType<?>> ENTITIES = PollinatedRegistry.create(Registry.ENTITY_TYPE, TolerableCreepers.MOD_ID);

    public static final Supplier<EntityType<CreeperSpores>> CREEPER_SPORES = ENTITIES.register("creeper_spores", () -> EntityType.Builder.<CreeperSpores>of(CreeperSpores::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).build("creeper_spores"));
    public static final Supplier<EntityType<Creepie>> CREEPIE = ENTITIES.register("creepie", () -> EntityType.Builder.<Creepie>of(Creepie::new, MobCategory.MONSTER).sized(0.625F, 0.875F).clientTrackingRange(8).build("creepie"));
    public static final Supplier<EntityType<PrimedSporeBarrel>> SPORE_BARREL = ENTITIES.register("spore_barrel", () -> EntityType.Builder.<PrimedSporeBarrel>of(PrimedSporeBarrel::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10).build("spore_barrel"));
}
