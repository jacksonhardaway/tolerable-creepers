package gg.moonflower.tolerablecreepers.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class SporeSackBlock extends Block {
    public SporeSackBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        Random random = level.random;
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            BlockPos neighborPos = blockPos.relative(direction);
            if (!level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) {
                Direction.Axis axis = direction.getAxis();
                double xOffset = axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) random.nextFloat();
                double yOffset = axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) random.nextFloat();
                double zOffset = axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) random.nextFloat();
                //level.addParticle(/*creeper spores or spore sprinkles*/, (double) blockPos.getX() + xOffset, (double) blockPos.getY() + yOffset, (double) blockPos.getZ() + zOffset, 0.0D, 0.0D, 0.0D);
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }
}
