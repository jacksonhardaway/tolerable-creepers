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
        nineBlockStorageRecipes(consumer, TCItems.CREEPER_SPORES.get(), TCBlocks.SPORE_BARREL.get(), TolerableCreepers.MOD_ID + ":" + getSimpleRecipeName(TCItems.CREEPER_SPORES.get()), null, TolerableCreepers.MOD_ID + ":" + getSimpleRecipeName(TCBlocks.SPORE_BARREL.get()), null);
//        bomb(consumer, TCItems.FIRE_BOMB.get(), Items.GUNPOWDER);
//        bomb(consumer, TCItems.SPORE_BOMB.get(), TCItems.CREEPER_SPORES.get());
//        ShapelessRecipeBuilder.shapeless(TCItems.MISCHIEF_ARROW.get(), 2).requires(Items.ARROW, 2).requires(TCItems.CREEPER_SPORES.get()).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, new ResourceLocation(getSimpleRecipeName(TCItems.MISCHIEF_ARROW.get())));
        ShapelessRecipeBuilder.shapeless(Items.LIME_DYE, 3).requires(TCItems.CREEPER_SPORES.get(), 2).requires(Items.EGG).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, new ResourceLocation(TolerableCreepers.MOD_ID, getConversionRecipeName(Items.LIME_DYE, TCItems.CREEPER_SPORES.get())));
        ShapelessRecipeBuilder.shapeless(Items.GUNPOWDER, 2).requires(TCItems.CREEPER_SPORES.get(), 4).requires(Items.CHARCOAL).unlockedBy(getHasName(TCItems.CREEPER_SPORES.get()), has(TCItems.CREEPER_SPORES.get())).save(consumer, new ResourceLocation(TolerableCreepers.MOD_ID, getConversionRecipeName(Items.GUNPOWDER, TCItems.CREEPER_SPORES.get())));
    }

    public static void bomb(Consumer<FinishedRecipe> consumer, ItemLike result, ItemLike ingredient) {
        ShapedRecipeBuilder.shaped(result, 2).define('#', Items.STRING).define('G', ingredient).pattern("  #").pattern("GG ").pattern("GG ").group("bombs").unlockedBy(getHasName(ingredient), has(ingredient)).save(consumer, new ResourceLocation(TolerableCreepers.MOD_ID, getSimpleRecipeName(result)));
    }

    private static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2) {
        nineBlockStorageRecipes(consumer, itemLike, itemLike2, getSimpleRecipeName(itemLike2), null, getSimpleRecipeName(itemLike), null);
    }

    private static void nineBlockStorageRecipesWithCustomPacking(
            Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, String string2
    ) {
        nineBlockStorageRecipes(consumer, itemLike, itemLike2, string, string2, getSimpleRecipeName(itemLike), null);
    }

    private static void nineBlockStorageRecipesRecipesWithCustomUnpacking(
            Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, String string2
    ) {
        nineBlockStorageRecipes(consumer, itemLike, itemLike2, getSimpleRecipeName(itemLike2), null, string, string2);
    }

    private static void nineBlockStorageRecipes(
            Consumer<FinishedRecipe> consumer, ItemLike itemLike, ItemLike itemLike2, String string, @Nullable String string2, String string3, @Nullable String string4
    ) {
        ShapelessRecipeBuilder.shapeless(itemLike, 9)
                .requires(itemLike2)
                .group(string4)
                .unlockedBy(getHasName(itemLike2), has(itemLike2))
                .save(consumer, new ResourceLocation(string3));
        ShapedRecipeBuilder.shaped(itemLike2)
                .define('#', itemLike)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .group(string2)
                .unlockedBy(getHasName(itemLike), has(itemLike))
                .save(consumer, new ResourceLocation(string));
    }

    private static String getHasName(ItemLike itemLike) {
        return "has_" + getItemName(itemLike);
    }

    private static String getItemName(ItemLike itemLike) {
        return Registry.ITEM.getKey(itemLike.asItem()).getPath();
    }

    private static String getSimpleRecipeName(ItemLike itemLike) {
        return getItemName(itemLike);
    }

    private static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
        return getItemName(itemLike) + "_from_" + getItemName(itemLike2);
    }

    private static String getSmeltingRecipeName(ItemLike itemLike) {
        return getItemName(itemLike) + "_from_smelting";
    }

    private static String getBlastingRecipeName(ItemLike itemLike) {
        return getItemName(itemLike) + "_from_blasting";
    }
}
