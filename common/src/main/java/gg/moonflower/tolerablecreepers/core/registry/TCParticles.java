package gg.moonflower.tolerablecreepers.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;

public class TCParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(TolerableCreepers.MOD_ID, Registry.PARTICLE_TYPE_REGISTRY);

    public static final Supplier<SimpleParticleType> CREEPER_SPORES = PARTICLES.register("creeper_spores", () -> new SimpleParticleType(false) {
    });
}
