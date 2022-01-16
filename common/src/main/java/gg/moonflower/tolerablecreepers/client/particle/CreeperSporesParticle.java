package gg.moonflower.tolerablecreepers.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class CreeperSporesParticle extends TextureSheetParticle {
    public static final float FRICTION = 0.6F;
    private final SpriteSet sprites;
    //TODO gravity will mess up positive, something here is attenuating my particles!!
    protected CreeperSporesParticle(ClientLevel clientLevel, double x, double y, double z, double xVelocity, double yVelocity, double zVelocity, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);
        this.gravity = 0.01F; //TODO tweak
        this.friction = FRICTION;
        this.sprites = spriteSet;
        this.xd = xVelocity;
        this.yd = yVelocity;
        this.zd = zVelocity;
        //this.hasPhysics = true;
        this.quadSize = 0.2F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
        this.lifetime = (int)(20.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D)) + 2; //TODO 64 change value? was originally 16
        this.setSpriteFromAge(spriteSet);
    }

    public void tick() {
        //TODO just override tick method
        /*if (Math.abs(this.xd) > 0.001 || Math.abs(this.zd) > 0.001) //might adjust values
            age--;*/
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double xVelocity, double yVelocity, double zVelocity) {
            return new CreeperSporesParticle(clientLevel, x, y, z, xVelocity, yVelocity, zVelocity, this.sprites);
        }
    }
}
