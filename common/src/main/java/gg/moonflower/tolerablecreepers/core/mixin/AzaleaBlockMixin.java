package gg.moonflower.tolerablecreepers.core.mixin;

import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AzaleaBlock.class)
public abstract class AzaleaBlockMixin extends BushBlock {

    protected AzaleaBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        if (collisionContext instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Creepie creepie && creepie.isHiding())
            return Shapes.empty();
        return super.getCollisionShape(state, level, pos, collisionContext);
    }
}
