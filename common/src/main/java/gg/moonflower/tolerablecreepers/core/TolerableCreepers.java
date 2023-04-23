package gg.moonflower.tolerablecreepers.core;

import gg.moonflower.pollen.api.datagen.provider.loot_table.PollinatedLootTableProvider;
import gg.moonflower.pollen.api.datagen.provider.model.PollinatedModelProvider;
import gg.moonflower.pollen.api.event.events.entity.EntityEvents;
import gg.moonflower.pollen.api.event.events.registry.client.ParticleFactoryRegistryEvent;
import gg.moonflower.pollen.api.event.events.world.ExplosionEvents;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.EntityAttributeRegistry;
import gg.moonflower.pollen.api.registry.client.EntityRendererRegistry;
import gg.moonflower.pollen.api.registry.client.ItemPredicateRegistry;
import gg.moonflower.pollen.api.registry.client.ModelRegistry;
import gg.moonflower.pollen.api.registry.content.CompostablesRegistry;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.tolerablecreepers.client.render.*;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.common.entity.FireBomb;
import gg.moonflower.tolerablecreepers.common.entity.SporeBomb;
import gg.moonflower.tolerablecreepers.core.mixin.MobAccessor;
import gg.moonflower.tolerablecreepers.core.registry.*;
import gg.moonflower.tolerablecreepers.datagen.*;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class TolerableCreepers {

    public static final String MOD_ID = "tolerablecreepers";
    public static final Platform PLATFORM = Platform.builder(MOD_ID)
            .clientInit(() -> TolerableCreepers::onClientInit)
            .clientPostInit(() -> TolerableCreepers::onClientPostInit)
            .commonInit(TolerableCreepers::onCommonInit)
            .commonPostInit(TolerableCreepers::onCommonPostInit)
            .dataInit(TolerableCreepers::onDataInit)
            .build();
    private static final Logger LOGGER = LogManager.getLogger();

    private static void onClientInit() {
        ParticleFactoryRegistryEvent.EVENT.register(TCParticles::registerParticles);
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/fire_bomb"));
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/spore_bomb"));

        EntityRendererRegistry.register(TCEntities.CREEPER_SPORES, NoopRenderer::new);
        EntityRendererRegistry.register(TCEntities.CREEPIE, CreepieRenderer::new);
        EntityRendererRegistry.register(TCEntities.SPORE_BARREL, SporeBarrelRenderer::new);
        EntityRendererRegistry.register(TCEntities.MISCHIEF_ARROW, MischiefArrowRenderer::new);
        EntityRendererRegistry.register(TCEntities.FIRE_BOMB, FireBombRenderer::new);
        EntityRendererRegistry.register(TCEntities.SPORE_BOMB, SporeBombRenderer::new);

        ItemPredicateRegistry.register(
                Items.CROSSBOW,
                new ResourceLocation(MOD_ID, "mischief_arrow"),
                (itemStack, clientLevel, livingEntity, i) -> livingEntity != null
                        && CrossbowItem.isCharged(itemStack)
                        && CrossbowItem.containsChargedProjectile(itemStack, TCItems.MISCHIEF_ARROW.get())
                        ? 1.0F
                        : 0.0F
        );
    }

    private static void onClientPostInit(Platform.ModSetupContext ctx) {
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/fire_bomb"));
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/spore_bomb"));
    }

    private static void onCommonInit() {
        TCItems.ITEMS.register(TolerableCreepers.PLATFORM);
        TCBlocks.BLOCKS.register(TolerableCreepers.PLATFORM);
        TCEntities.ENTITIES.register(TolerableCreepers.PLATFORM);
        TCParticles.PARTICLES.register(TolerableCreepers.PLATFORM);

        ExplosionEvents.DETONATE.register((level, explosion, entityList) -> {
            entityList.removeIf(entity -> !(entity instanceof LivingEntity || entity.getType().is(TCTags.EXPLOSION_PRONE)) || entity.getType().is(TCTags.EXPLOSION_IMMUNE));
            if (explosion.getSourceMob() instanceof Creeper creeper) {
                explosion.getToBlow().clear();
                if (creeper.getType() != EntityType.CREEPER)
                    return;
                boolean day = level.getBrightness(LightLayer.SKY, creeper.blockPosition()) > 10 && level.isDay();
                Random random = creeper.getRandom();
                CreeperSpores creeperSpores = new CreeperSpores(level, creeper.getX(), creeper.getY() + 0.01, creeper.getZ(), Math.round(((day ? 1 : 2) + random.nextInt(day ? 2 : 3)) * creeper.getHealth() / creeper.getMaxHealth()), creeper.isPowered());
                if (!creeper.isInvisible())
                    creeperSpores.setOwner(creeper);
                level.addFreshEntity(creeperSpores);
            }
        });
        EntityEvents.JOIN.register(((entity, level) -> {
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
            return true;
        }));

        EntityAttributeRegistry.register(TCEntities.CREEPIE, Creepie::createAttributes);
    }

    private static void onCommonPostInit(Platform.ModSetupContext ctx) {
        ctx.enqueueWork(() -> {
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
                    level.getLevel().gameEvent(GameEvent.ENTITY_PLACE, level.getPos());
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
        });
        CompostablesRegistry.register(TCItems.CREEPER_SPORES.get(), 0.65F);
    }

    private static void onDataInit(Platform.DataSetupContext ctx) {
        DataGenerator generator = ctx.getGenerator();
        PollinatedModContainer container = ctx.getMod();
        generator.addProvider(new TCLanguageProvider(generator, container));
        generator.addProvider(new TCRecipeProvider(generator));
        BlockTagsProvider blockTagProvider = new TCBlockTagProvider(generator, container);
        generator.addProvider(blockTagProvider);
        generator.addProvider(new TCItemTagProvider(generator, container, blockTagProvider));
        generator.addProvider(new TCEntityTypeTagProvider(generator, container));
        generator.addProvider(new PollinatedLootTableProvider(generator)
                .add(LootContextParamSets.BLOCK, new TCBlockLootProvider(container))
                .add(LootContextParamSets.ENTITY, new TCEntityLootProvider()));
        PollinatedModelProvider modelProvider = new PollinatedModelProvider(generator, container);
        modelProvider.addGenerator(TCBlockModelProvider::new);
        modelProvider.addGenerator(TCItemModelProvider::new);
        generator.addProvider(modelProvider);
    }
}
