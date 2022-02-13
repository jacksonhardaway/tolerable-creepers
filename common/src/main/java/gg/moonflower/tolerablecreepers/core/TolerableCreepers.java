package gg.moonflower.tolerablecreepers.core;

import gg.moonflower.pollen.api.event.events.entity.EntityEvents;
import gg.moonflower.pollen.api.event.events.registry.client.ParticleFactoryRegistryEvent;
import gg.moonflower.pollen.api.event.events.world.ExplosionEvents;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.EntityAttributeRegistry;
import gg.moonflower.pollen.api.registry.client.EntityRendererRegistry;
import gg.moonflower.pollen.api.registry.client.ModelRegistry;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.tolerablecreepers.client.render.CreepieRenderer;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.common.entity.Creepie;
import gg.moonflower.tolerablecreepers.core.mixin.MobAccessor;
import gg.moonflower.tolerablecreepers.core.registry.*;
import gg.moonflower.tolerablecreepers.datagen.TCLanguageProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.LightLayer;

import java.util.Random;

public class TolerableCreepers {

    public static final String MOD_ID = "tolerablecreepers";
    public static final Platform PLATFORM = Platform.builder(MOD_ID)
            .clientInit(TolerableCreepers::onClientInit)
            .clientPostInit(TolerableCreepers::onClientPostInit)
            .commonInit(TolerableCreepers::onCommonInit)
            .commonPostInit(TolerableCreepers::onCommonPostInit)
            .dataInit(TolerableCreepers::onDataInit)
            .build();

    private static void onClientInit() {
        ParticleFactoryRegistryEvent.EVENT.register(TCParticles::registerParticles);
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/fire_bomb"));
        ModelRegistry.registerSpecial(new ResourceLocation(MOD_ID, "entity/spore_bomb"));
    }

    private static void onClientPostInit(Platform.ModSetupContext ctx) {
        EntityRendererRegistry.register(TCEntities.CREEPER_SPORES, NoopRenderer::new);
        EntityRendererRegistry.register(TCEntities.CREEPIE, CreepieRenderer::new);
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
                CreeperSpores creeperSpores = new CreeperSpores(level, creeper.getX(), creeper.getY(), creeper.getZ(), Math.round(((day ? 1 : 2) + random.nextInt(day ? 2 : 3)) * creeper.getHealth() / creeper.getMaxHealth()), creeper.isPowered());
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
            //else if (mob instanceof Villager)
            //creepie avoid goal
            return true;
        }));
    }

    private static void onCommonPostInit(Platform.ModSetupContext ctx) {
        EntityAttributeRegistry.register(TCEntities.CREEPIE, Creepie::createAttributes);
    }

    private static void onDataInit(Platform.DataSetupContext ctx) {
        DataGenerator generator = ctx.getGenerator();
        PollinatedModContainer container = ctx.getMod();
        generator.addProvider(new TCLanguageProvider(generator, container));
    }
}
