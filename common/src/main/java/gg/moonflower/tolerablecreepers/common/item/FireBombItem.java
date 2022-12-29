package gg.moonflower.tolerablecreepers.common.item;

import gg.moonflower.tolerablecreepers.common.entity.FireBomb;
import gg.moonflower.tolerablecreepers.common.entity.MischiefArrow;
import gg.moonflower.tolerablecreepers.common.entity.ThrowableBomb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireBombItem extends BombItem {

    public FireBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBomb createBomb(LivingEntity entity, Level level) {
        return new FireBomb(entity, level);
    }
}
