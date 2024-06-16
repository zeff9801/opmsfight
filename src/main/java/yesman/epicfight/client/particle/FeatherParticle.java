package yesman.epicfight.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FeatherParticle extends SpriteTexturedParticle {
	protected FeatherParticle(ClientWorld level, double x, double y, double z, double dx, double dy, double dz) {
		super(level, x, y, z, dx, dy, dz);
		
		this.lifetime = 8 + this.random.nextInt(22);
		this.hasPhysics = true;
		this.gravity = 0.4F;
		//this.friction = 0.8F;
		
		float roll = this.random.nextFloat() * 180F;
		this.oRoll = roll;
		this.roll = roll;
		
		this.xd = dx;
		this.yd = dy;
		this.zd = dz;
	}
	
	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		private final IAnimatedSprite sprite;

		public Provider(IAnimatedSprite sprite) {
			this.sprite = sprite;
		}
		
		@Override
		public Particle createParticle(BasicParticleType particleType, ClientWorld level, double x, double y, double z, double dx, double dy, double dz) {
			FeatherParticle featuerparticle = new FeatherParticle(level, x, y, z, dx, dy, dz);
			featuerparticle.pickSprite(this.sprite);
			return featuerparticle;
		}
	}
}