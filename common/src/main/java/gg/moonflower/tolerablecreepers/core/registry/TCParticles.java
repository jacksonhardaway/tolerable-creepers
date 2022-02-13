package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.event.events.registry.client.ParticleFactoryRegistryEvent;
import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.client.particle.CreeperSporesParticle;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;

public class TCParticles {

    public static final PollinatedRegistry<ParticleType<?>> PARTICLES = PollinatedRegistry.create(Registry.PARTICLE_TYPE, TolerableCreepers.MOD_ID);

    public static final Supplier<SimpleParticleType> CREEPER_SPORES = PARTICLES.register("creeper_spores", () -> new SimpleParticleType(false));

    public static void registerParticles(ParticleFactoryRegistryEvent.Registry registry) {
        registry.register(CREEPER_SPORES.get(), CreeperSporesParticle.Provider::new);
    }
}
