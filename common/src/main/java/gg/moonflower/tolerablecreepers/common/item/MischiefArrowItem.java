package gg.moonflower.tolerablecreepers.common.item;

import gg.moonflower.tolerablecreepers.common.entity.MischiefArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MischiefArrowItem extends ArrowItem {

    public MischiefArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity entity) {
        return new MischiefArrow(level, entity);
    }
}
