package gg.moonflower.tolerablecreepers.core;

import gg.moonflower.pollen.api.event.events.entity.EntityEvents;
import gg.moonflower.pollen.api.event.events.registry.client.ParticleFactoryRegistryEvent;
import gg.moonflower.pollen.api.event.events.world.ExplosionEvents;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.client.EntityRendererRegistry;
import gg.moonflower.tolerablecreepers.common.entity.CreeperSpores;
import gg.moonflower.tolerablecreepers.core.mixin.MobAccessor;
import gg.moonflower.tolerablecreepers.core.registry.*;
import net.minecraft.client.renderer.entity.NoopRenderer;
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
            .build();

    public static void onClientInit() {
        ParticleFactoryRegistryEvent.EVENT.register(TCParticles::registerParticles);
    }

    public static void onClientPostInit(Platform.ModSetupContext ctx) {
        EntityRendererRegistry.register(TCEntities.CREEPER_SPORES, NoopRenderer::new);
    }

    public static void onCommonInit() {
        TCItems.ITEMS.register(TolerableCreepers.PLATFORM);
        TCBlocks.BLOCKS.register(TolerableCreepers.PLATFORM);
        TCEntities.ENTITIES.register(TolerableCreepers.PLATFORM);
        TCParticles.PARTICLES.register(TolerableCreepers.PLATFORM);
        ExplosionEvents.DETONATE.register((level, explosion, entityList) -> {
            entityList.removeIf(entity -> !(entity instanceof LivingEntity || entity.getType().is(TCTags.EXPLOSION_PRONE)) || entity.getType().is(TCTags.EXPLOSION_IMMUNE));
            if (explosion.getSourceMob() instanceof Creeper creeper) {
                explosion.getToBlow().clear();
                boolean day = level.getBrightness(LightLayer.SKY, creeper.blockPosition()) > 10 && level.isDay();
                Random random = creeper.getRandom();
                CreeperSpores creeperSpores = new CreeperSpores(level, creeper.getX(), creeper.getY(), creeper.getZ(), Math.round(((day ? 1 : 2) + random.nextInt(day ? 2 : 3)) * creeper.getHealth() / creeper.getMaxHealth()), creeper.isPowered());
                level.addFreshEntity(creeperSpores);
            }
        });
        EntityEvents.JOIN.register(((entity, level) -> {
            if (entity instanceof Mob mob) {
                if (mob instanceof IronGolem) {
                    GoalSelector targetSelector = ((MobAccessor) mob).getTargetSelector();
                    targetSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g -> g instanceof NearestAttackableTargetGoal).findAny().ifPresent(g -> {
                        targetSelector.removeGoal(g);
                        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Mob.class, 5, false, false, e -> e instanceof Enemy));
                    });
                }
                //else if (mob instanceof Villager)
                //creepie avoid goal
            }
            return true;
        }));
    }

    public static void onCommonPostInit(Platform.ModSetupContext ctx) {
    }
}
