package gg.moonflower.tolerablecreepers.datagen;

import com.google.gson.JsonElement;
import gg.moonflower.pollen.api.datagen.provider.model.PollinatedBlockModelGenerator;
import gg.moonflower.pollen.api.datagen.provider.model.PollinatedItemModelGenerator;
import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TCItemModelProvider extends PollinatedItemModelGenerator {

    public TCItemModelProvider(Consumer<BlockStateGenerator> blockStateOutput, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, Consumer<Item> skippedAutoModelsOutput) {
        super(modelOutput);
    }

    @Override
    public void run() {
        this.generateFlatItem(TCItems.CREEPER_SPORES.get(), ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(TCItems.MISCHIEF_ARROW.get(), ModelTemplates.FLAT_ITEM);
    }
}
