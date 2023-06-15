package gg.moonflower.tolerablecreepers.core;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.ExplosionEvent;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.common.entity.FireBomb;
import gg.moonflower.tolerablecreepers.common.entity.MischiefArrow;
import gg.moonflower.tolerablecreepers.common.entity.PrimedSporeBarrel;
import gg.moonflower.tolerablecreepers.common.entity.SporeBomb;
import gg.moonflower.tolerablecreepers.core.mixin.MobAccessor;
import gg.moonflower.tolerablecreepers.core.registry.TCBlocks;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TolerableCreepers {

    public static final String MOD_ID = "tolerablecreepers";
    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        TCBlocks.BLOCKS.register();
        TCItems.ITEMS.register();
        TCEntities.ENTITIES.register();
        TCParticles.PARTICLES.register();

        ExplosionEvent.DETONATE.register((level, explosion, entityList) -> {
            entityList.removeIf(entity -> !(entity instanceof LivingEntity || entity.getType().is(TCTags.EXPLOSION_PRONE)) || entity.getType().is(TCTags.EXPLOSION_IMMUNE));
            if (explosion.getSourceMob() instanceof Creeper creeper) {
                explosion.getToBlow().clear();
                if (creeper.getType() != EntityType.CREEPER)
                    return;
                boolean day = level.getBrightness(LightLayer.SKY, creeper.blockPosition()) > 10 && level.isDay();
                RandomSource random = creeper.getRandom();
                CreeperSpores creeperSpores = new CreeperSpores(level, creeper.getX(), creeper.getY() + 0.01, creeper.getZ(), Math.round(((day ? 1 : 2) + random.nextInt(day ? 2 : 3)) * creeper.getHealth() / creeper.getMaxHealth()), creeper.isPowered());
                if (!creeper.isInvisible())
                    creeperSpores.setOwner(creeper);
                level.addFreshEntity(creeperSpores);
            }
        });
        EntityEvent.ADD.register(((entity, level) -> {
            if (entity instanceof IronGolem golem) {
                GoalSelector targetSelector = ((MobAccessor) entity).getTargetSelector();
                targetSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g -> g instanceof NearestAttackableTargetGoal).findAny().ifPresent(g -> {
                    targetSelector.removeGoal(g);
                    targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(golem, Mob.class, 5, false, false, e -> e instanceof Enemy));
                });
            }
            if (entity instanceof Ocelot || entity instanceof Cat) {
                GoalSelector targetSelector = ((MobAccessor) entity).getTargetSelector();
                targetSelector.addGoal(1, new NearestAttackableTargetGoal<>((Mob) entity, Creepie.class, false));
            }
            //else if (mob instanceof Villager)
            //creepie avoid goal
            return EventResult.pass();
        }));

        EntityAttributeRegistry.register(TCEntities.CREEPIE, Creepie::createAttributes);
    }

    public static void postInit() {
        DispenserBlock.registerBehavior(TCItems.CREEPER_SPORES.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource level, ItemStack stack) {
                Direction direction = level.getBlockState().getValue(DispenserBlock.FACING);

                try {
                    TCEntities.CREEPIE.get().spawn(level.getLevel(), stack, null, level.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
                } catch (Exception var6) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", level.getPos(), var6);
                    return ItemStack.EMPTY;
                }

                stack.shrink(1);
                level.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, level.getPos());
                return stack;
            }
        });
        DispenserBlock.registerBehavior(TCItems.FIRE_BOMB.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return new FireBomb(level, position.x(), position.y(), position.z());
            }

            @Override
            protected float getUncertainty() {
                return super.getUncertainty() * 0.5F;
            }

            @Override
            protected float getPower() {
                return super.getPower() * 1.25F;
            }
        });
        DispenserBlock.registerBehavior(TCItems.SPORE_BOMB.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return new SporeBomb(level, position.x(), position.y(), position.z());
            }

            @Override
            protected float getUncertainty() {
                return super.getUncertainty() * 0.5F;
            }

            @Override
            protected float getPower() {
                return super.getPower() * 1.25F;
            }
        });
        DispenserBlock.registerBehavior(TCItems.MISCHIEF_ARROW.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                MischiefArrow arrow = new MischiefArrow(level, position.x(), position.y(), position.z());
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrow;
            }
        });
        DispenserBlock.registerBehavior(TCBlocks.SPORE_BARREL.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource arg, ItemStack arg2) {
                Level level = arg.getLevel();
                BlockPos blockpos = arg.getPos().relative(arg.getBlockState().getValue(DispenserBlock.FACING));
                PrimedSporeBarrel sporeBarrel = new PrimedSporeBarrel(level, (double) blockpos.getX() + 0.5, blockpos.getY(), (double) blockpos.getZ() + 0.5, null);
                level.addFreshEntity(sporeBarrel);
                level.playSound(null, sporeBarrel.getX(), sporeBarrel.getY(), sporeBarrel.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                arg2.shrink(1);
                return arg2;
            }
        });
//        CompostablesRegistry.register(TCItems.CREEPER_SPORES.get(), 0.65F);
    }

//    private static void onDataInit(Platform.DataSetupContext ctx) {
//        DataGenerator generator = ctx.getGenerator();
//        PollinatedModContainer container = ctx.getMod();
//        generator.addProvider(new TCLanguageProvider(generator, container));
//        generator.addProvider(new TCRecipeProvider(generator));
//        BlockTagsProvider blockTagProvider = new TCBlockTagProvider(generator, container);
//        generator.addProvider(blockTagProvider);
//        generator.addProvider(new TCItemTagProvider(generator, container, blockTagProvider));
//        generator.addProvider(new TCEntityTypeTagProvider(generator, container));
//        generator.addProvider(new PollinatedLootTableProvider(generator)
//                .add(LootContextParamSets.BLOCK, new TCBlockLootProvider(container))
//                .add(LootContextParamSets.ENTITY, new TCEntityLootProvider()));
//        PollinatedModelProvider modelProvider = new PollinatedModelProvider(generator, container);
//        modelProvider.addGenerator(TCBlockModelProvider::new);
//        modelProvider.addGenerator(TCItemModelProvider::new);
//        generator.addProvider(modelProvider);
//    }
}
