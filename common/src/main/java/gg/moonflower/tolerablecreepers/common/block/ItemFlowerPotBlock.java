package gg.moonflower.tolerablecreepers.common.block;

import gg.moonflower.tolerablecreepers.core.mixin.FlowerPotBlockAccessor;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class ItemFlowerPotBlock extends FlowerPotBlock {

    public ItemFlowerPotBlock(Properties properties) {
        super(Blocks.AIR, properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(TCItems.CREEPER_SPORES.get());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        ItemStack stack = player.getItemInHand(hand);
        boolean remove = !(stack.getItem() instanceof BlockItem blockItem) || !FlowerPotBlockAccessor.getPottedByContent().containsKey(blockItem.getBlock());
        if (remove && !stack.is(TCItems.CREEPER_SPORES.get())) {
            ItemStack contentStack = new ItemStack(TCItems.CREEPER_SPORES.get());
            if (stack.isEmpty()) {
                player.setItemInHand(hand, contentStack);
            } else if (!player.addItem(contentStack)) {
                player.drop(contentStack, false);
            }

            level.setBlock(pos, Blocks.FLOWER_POT.defaultBlockState(), 3);

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.CONSUME;
        }
    }
}
