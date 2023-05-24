package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.wrapper.v1.PollinatedBlockRegistry;
import gg.moonflower.tolerablecreepers.common.block.SporeBarrelBlock;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class TCBlocks {

    public static final PollinatedBlockRegistry BLOCKS = PollinatedBlockRegistry.create(TCItems.ITEMS);

    public static final Supplier<Block> SPORE_BARREL = BLOCKS.registerWithItem("spore_barrel", () -> new SporeBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));

}
