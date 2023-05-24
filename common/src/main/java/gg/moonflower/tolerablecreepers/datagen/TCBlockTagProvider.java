//package gg.moonflower.tolerablecreepers.datagen;
//
//import gg.moonflower.pollen.api.datagen.provider.tags.PollinatedBlockTagsProvider;
//import gg.moonflower.pollen.api.util.PollinatedModContainer;
//import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
//import gg.moonflower.tolerablecreepers.core.registry.TCTags;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.world.level.block.Blocks;
//
//public class TCBlockTagProvider extends PollinatedBlockTagsProvider {
//
//    public TCBlockTagProvider(DataGenerator generator, PollinatedModContainer container) {
//        super(generator, container);
//    }
//
//    @Override
//    protected void addTags() {
//        super.addTags();
//        this.tag(TCTags.CREEPIE_REPELLENTS);
//        this.tag(TCTags.CREEPIE_HIDING_SPOTS).add(Blocks.AZALEA, Blocks.FLOWERING_AZALEA);
//        this.tag(TCTags.CREEPIE_PARTY_SPOTS).add(Blocks.JUKEBOX, Blocks.SPORE_BLOSSOM);
//        this.tag(TCTags.FIRE_BOMB_EXPLODE).add(Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE);
//        this.tag(BlockTags.MINEABLE_WITH_AXE).add(TCBlocks.SPORE_BARREL.get());
//    }
//}
