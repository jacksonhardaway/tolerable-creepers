package gg.moonflower.tolerablecreepers.common.item;

import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

public class CreeperSporesItem extends Item {
    public CreeperSporesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        //TODO change sound event later
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        Random random = player.getRandom();
        if (!level.isClientSide) {
            CreeperSpores creeperSpores = new CreeperSpores(player, level, random.nextFloat() > 0.25F ? 2 : random.nextFloat() > 0.01F ? 1 : 0, false);
            creeperSpores.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(creeperSpores);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild)
            stack.shrink(1);
        player.getCooldowns().addCooldown(TCItems.CREEPER_SPORES.get(), 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
