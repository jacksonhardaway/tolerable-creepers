package gg.moonflower.tolerablecreepers.core.mixin;

import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public class FlowerPotBlockMixin {

    @Unique
    private ItemStack captureStack;

    @Inject(method = "use", at = @At("HEAD"))
    public void captureItem(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        this.captureStack = player.getItemInHand(interactionHand);
    }

    @Inject(method = "use", at = @At("RETURN"))
    public void clearItem(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        this.captureStack = null;
    }

    @ModifyVariable(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, shift = At.Shift.BY, by = 2), ordinal = 1)
    public BlockState modifyState(BlockState instance) {
        if (this.captureStack.is(TCItems.CREEPER_SPORES.get())) {
            return TCBlocks.POTTED_CREEPER_SPORES.get().defaultBlockState();
        }
        return instance;
    }
}
