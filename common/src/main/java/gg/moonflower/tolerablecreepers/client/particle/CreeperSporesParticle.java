package gg.moonflower.tolerablecreepers.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class CreeperSporesParticle extends TextureSheetParticle {

    public static final float FRICTION = 0.6F;
    private final SpriteSet sprites;
    private final int spriteAge;

    protected CreeperSporesParticle(ClientLevel clientLevel, double x, double y, double z, double xVelocity, double yVelocity, double zVelocity, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);
        this.gravity = 0.1F;
        this.friction = FRICTION;
        this.sprites = spriteSet;
        this.xd = xVelocity;
        this.yd = yVelocity;
        this.zd = zVelocity;
        this.quadSize = 0.2F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
        this.lifetime = (int) (20.0D / (this.random.nextFloat() * 0.8D + 0.2D)) + 2; //TODO 64 change value? was originally 16
        this.spriteAge = this.random.nextInt(this.lifetime / 2);
        this.roll = this.oRoll = this.random.nextFloat() * (float) Math.PI * 2.0F;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.oRoll = this.roll;
            this.roll += (float) (Math.PI * Math.min(0.5, this.yd) * 2.0F);

            this.yd -= 0.04 * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }

    @Override
    public void setSpriteFromAge(SpriteSet spriteSet) {
        if (!this.removed)
            this.setSprite(spriteSet.get(Math.min(this.age, this.lifetime - this.spriteAge), this.lifetime - this.spriteAge));
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
