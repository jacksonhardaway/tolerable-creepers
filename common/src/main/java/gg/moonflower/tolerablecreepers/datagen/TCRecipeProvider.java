package gg.moonflower.tolerablecreepers.datagen;

import gg.moonflower.pollen.api.datagen.provider.PollinatedRecipeProvider;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TCRecipeProvider extends PollinatedRecipeProvider {

    public TCRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        nineBlockStorageRecipes(consumer, TCItems.CREEPER_SPORES.get(), TCBlocks.SPORE_BARREL.get(), getSimpleRecipeName(TCItems.CREEPER_SPORES.get()), null, getSimpleRecipeName(TCBlocks.SPORE_BARREL.get()), null);
//        bomb(consumer, TCItems.FIRE_BOMB.get(), Items.GUNPOWDER);
//        bomb(consumer, TCItems.SPORE_BOMB.get(), TCItems.CREEPER_SPORES.get());
//        ShapelessRecipeBuilder.shapeless(TCItems.MISCHIEF_ARROW.get(), 2).requires(Items.ARROW, 2).requires(TCItems.CREEPER_SPORES.get()).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, new ResourceLocation(getSimpleRecipeName(TCItems.MISCHIEF_ARROW.get())));
        ShapelessRecipeBuilder.shapeless(Items.LIME_DYE, 3).requires(TCItems.CREEPER_SPORES.get(), 2).requires(Items.EGG).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, getConversionRecipeName(Items.LIME_DYE, TCItems.CREEPER_SPORES.get()));
        ShapelessRecipeBuilder.shapeless(Items.GUNPOWDER, 2).requires(TCItems.CREEPER_SPORES.get(), 4).requires(Items.CHARCOAL).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, getConversionRecipeName(Items.GUNPOWDER, TCItems.CREEPER_SPORES.get()));
    }

    public static void bomb(Consumer<FinishedRecipe> consumer, ItemLike result, ItemLike ingredient) {
        ShapedRecipeBuilder.shaped(result, 2).define('#', Items.STRING).define('G', ingredient).pattern("  #").pattern("GG ").pattern("GG ").group("bombs").unlockedBy(getHasName(ingredient), has(ingredient)).save(consumer, getSimpleRecipeName(result));
    }
}
