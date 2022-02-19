package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.PollinatedEntityRegistry;
import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.common.entity.ai.sensing.CreepieAttackablesSensor;
import gg.moonflower.tolerablecreepers.common.entity.ai.sensing.CreepieSpecificSensor;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

public class TCEntities {

    public static final PollinatedEntityRegistry ENTITIES = PollinatedRegistry.createEntity(TolerableCreepers.MOD_ID);

    public static final Supplier<EntityType<CreeperSpores>> CREEPER_SPORES = ENTITIES.register("creeper_spores", () -> EntityType.Builder.<CreeperSpores>of(CreeperSpores::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).build("creeper_spores"));
    public static final Supplier<EntityType<Creepie>> CREEPIE = ENTITIES.register("creepie", () -> EntityType.Builder.<Creepie>of(Creepie::new, MobCategory.MONSTER).sized(0.625F, 0.875F).clientTrackingRange(8).build("creepie"));

    public static final Supplier<SensorType<CreepieAttackablesSensor>> CREEPIE_ATTACKABLES_SENSOR = ENTITIES.registerSensorType("creepie_attackables_sensor", CreepieAttackablesSensor::new);
    public static final Supplier<SensorType<CreepieSpecificSensor>> CREEPIE_SPECIFIC_SENSOR = ENTITIES.registerSensorType("creepie_specific_sensor", CreepieSpecificSensor::new);

}
