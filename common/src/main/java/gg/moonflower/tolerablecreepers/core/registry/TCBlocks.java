package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.PollinatedBlockRegistry;
import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.common.block.ItemFlowerPotBlock;
import gg.moonflower.tolerablecreepers.common.block.SporeBarrelBlock;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

public class TCBlocks {

    public static final PollinatedBlockRegistry BLOCKS = PollinatedRegistry.createBlock(TCItems.ITEMS);

    public static final Supplier<Block> SPORE_BARREL = BLOCKS.registerWithItem("spore_barrel", () -> new SporeBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
    public static final Supplier<Block> POTTED_CREEPER_SPORES = BLOCKS.register("potted_creeper_spores", () -> new ItemFlowerPotBlock(BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));

}
