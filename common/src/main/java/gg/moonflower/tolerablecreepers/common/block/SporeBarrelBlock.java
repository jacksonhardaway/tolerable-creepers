package gg.moonflower.tolerablecreepers.common.block;

import gg.moonflower.tolerablecreepers.common.entity.PrimedSporeBarrel;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SporeBarrelBlock extends Block {

    public SporeBarrelBlock(Properties properties) {
        super(properties);
    }

    public static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        if (!level.isClientSide()) {
            PrimedTnt primedTnt = new PrimedSporeBarrel(level, (double) blockPos.getX() + 0.5, blockPos.getY(), (double) blockPos.getZ() + 0.5, livingEntity);
            level.addFreshEntity(primedTnt);
            level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(livingEntity, GameEvent.PRIME_FUSE, blockPos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        RandomSource random = level.getRandom();
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            BlockPos neighborPos = pos.relative(direction);
            if (!level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) {
                Direction.Axis axis = direction.getAxis();
                double xOffset = axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) random.nextFloat();
                double yOffset = axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) random.nextFloat();
                double zOffset = axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) random.nextFloat();
                level.addParticle(TCParticles.CREEPER_SPORES.get(), (double) pos.getX() + xOffset, (double) pos.getY() + yOffset, (double) pos.getZ() + zOffset, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE))
            return super.use(state, level, pos, player, hand, blockHitResult);

        explode(level, pos, player);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        Item item = itemStack.getItem();
        if (!player.isCreative()) {
            if (itemStack.is(Items.FLINT_AND_STEEL)) {
                itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(hand));
            } else {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(item));
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState2.is(blockState.getBlock()) && level.hasNeighborSignal(blockPos)) {
            explode(level, blockPos, null);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.hasNeighborSignal(blockPos)) {
            explode(level, blockPos, null);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (!level.isClientSide()) {
            PrimedTnt primedTnt = new PrimedSporeBarrel(level, (double) blockPos.getX() + 0.5, blockPos.getY(), (double) blockPos.getZ() + 0.5, explosion.getSourceMob());
            primedTnt.setFuse((short) (level.getRandom().nextInt(15) + 5));
            level.addFreshEntity(primedTnt);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(level, blockPos)) {
                explode(level, blockPos, entity instanceof LivingEntity ? (LivingEntity) entity : null);
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }
}
