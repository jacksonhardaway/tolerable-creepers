package gg.moonflower.tolerablecreepers.common.item;

import gg.moonflower.tolerablecreepers.common.entity.SporeBomb;
import gg.moonflower.tolerablecreepers.common.entity.ThrowableBomb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SporeBombItem extends BombItem {

    public SporeBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBomb createBomb(LivingEntity entity, Level level) {
        return new SporeBomb(entity, level);
    }
}
