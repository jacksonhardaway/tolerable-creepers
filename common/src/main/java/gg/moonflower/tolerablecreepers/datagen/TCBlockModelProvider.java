//package gg.moonflower.tolerablecreepers.datagen;
//
//import com.google.gson.JsonElement;
//import gg.moonflower.pollen.api.datagen.provider.model.PollinatedBlockModelGenerator;
//import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
//import net.minecraft.data.models.blockstates.BlockStateGenerator;
//import net.minecraft.data.models.model.TexturedModel;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Item;
//
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
//public class TCBlockModelProvider extends PollinatedBlockModelGenerator {
//
//    public TCBlockModelProvider(Consumer<BlockStateGenerator> blockStateOutput, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, Consumer<Item> skippedAutoModelsOutput) {
//        super(blockStateOutput, modelOutput, skippedAutoModelsOutput);
//    }
//
//    @Override
//    public void run() {
//        this.createTrivialBlock(TCBlocks.SPORE_BARREL.get(), TexturedModel.CUBE_TOP_BOTTOM);
//    }
//}
