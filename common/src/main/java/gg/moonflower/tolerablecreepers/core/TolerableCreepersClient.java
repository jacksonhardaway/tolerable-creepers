package gg.moonflower.tolerablecreepers.core;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import gg.moonflower.pollen.api.registry.render.v1.ModelRegistry;
import gg.moonflower.tolerablecreepers.client.particle.CreeperSporesParticle;
import gg.moonflower.tolerablecreepers.client.render.CreepieRenderer;
import gg.moonflower.tolerablecreepers.client.render.FireBombRenderer;
import gg.moonflower.tolerablecreepers.client.render.MischiefArrowRenderer;
import gg.moonflower.tolerablecreepers.client.render.SporeBarrelRenderer;
import gg.moonflower.tolerablecreepers.client.render.SporeBombRenderer;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import gg.moonflower.tolerablecreepers.core.registry.TCParticles;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;

public class TolerableCreepersClient {

    public static void init() {
        ParticleProviderRegistry.register(TCParticles.CREEPER_SPORES, CreeperSporesParticle.Provider::new);
        ModelRegistry.registerSpecial(new ResourceLocation(TolerableCreepers.MOD_ID, "entity/fire_bomb"));
        ModelRegistry.registerSpecial(new ResourceLocation(TolerableCreepers.MOD_ID, "entity/spore_bomb"));

        EntityRendererRegistry.register(TCEntities.CREEPER_SPORES, NoopRenderer::new);
        EntityRendererRegistry.register(TCEntities.CREEPIE, CreepieRenderer::new);
        EntityRendererRegistry.register(TCEntities.SPORE_BARREL, SporeBarrelRenderer::new);
        EntityRendererRegistry.register(TCEntities.MISCHIEF_ARROW, MischiefArrowRenderer::new);
        EntityRendererRegistry.register(TCEntities.FIRE_BOMB, FireBombRenderer::new);
        EntityRendererRegistry.register(TCEntities.SPORE_BOMB, SporeBombRenderer::new);

        ItemPropertiesRegistry.register(
                Items.CROSSBOW,
                new ResourceLocation(TolerableCreepers.MOD_ID, "mischief_arrow"),
                (itemStack, clientLevel, livingEntity, i) -> livingEntity != null
                        && CrossbowItem.isCharged(itemStack)
                        && CrossbowItem.containsChargedProjectile(itemStack, TCItems.MISCHIEF_ARROW.get())
                        ? 1.0F
                        : 0.0F
        );
    }

    public static void postInit() {
        ModelRegistry.registerSpecial(new ResourceLocation(TolerableCreepers.MOD_ID, "entity/fire_bomb"));
        ModelRegistry.registerSpecial(new ResourceLocation(TolerableCreepers.MOD_ID, "entity/spore_bomb"));
    }
}
