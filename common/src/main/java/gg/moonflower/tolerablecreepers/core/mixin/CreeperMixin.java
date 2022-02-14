package gg.moonflower.tolerablecreepers.core.mixin;

import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Entity implements CreeperExtension {

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    private CreeperMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tolerablecreepers$setPowered(boolean powered) {
        this.entityData.set(DATA_IS_POWERED, powered);
    }
}
