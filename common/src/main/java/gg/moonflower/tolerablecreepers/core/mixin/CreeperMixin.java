package gg.moonflower.tolerablecreepers.core.mixin;

import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.extension.CreeperExtension;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements CreeperExtension {

    @Unique
    private static final ResourceLocation DETONATE_LOOT_TABLE = new ResourceLocation(TolerableCreepers.MOD_ID, "entities/creeper_explode");

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    @Unique
    private boolean exploded;

    private CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tolerablecreepers$setPowered(boolean powered) {
        this.entityData.set(DATA_IS_POWERED, powered);
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
        this.exploded = this.getType() == EntityType.CREEPER && damageSource.isExplosion();
        super.dropFromLootTable(damageSource, bl);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.exploded ? DETONATE_LOOT_TABLE : super.getDefaultLootTable();
    }
}
