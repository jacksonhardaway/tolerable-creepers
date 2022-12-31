package gg.moonflower.tolerablecreepers.datagen;

import gg.moonflower.pollen.api.datagen.provider.loot_table.PollinatedLootGenerator;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class TCEntityLootProvider implements PollinatedLootGenerator {

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registry) {
        register(registry, TCEntities.CREEPIE.get(), LootTable.lootTable());
        registry.accept(new ResourceLocation(TolerableCreepers.MOD_ID, "entities/creeper_explode"), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                                LootItem.lootTableItem(TCItems.CREEPER_SPORES.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F)))
                        )
                ));
    }

    private static void register(BiConsumer<ResourceLocation, LootTable.Builder> registry, EntityType<?> entityType, LootTable.Builder lootTable) {
        registry.accept(entityType.getDefaultLootTable(), lootTable);
    }
}
