package yesman.epicfight.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TsunamiSplashParticle extends SpriteTexturedParticle {
	private final IAnimatedSprite sprites;

	TsunamiSplashParticle(ClientWorld p_108407_, double p_108408_, double p_108409_, double p_108410_, double p_108411_, double p_108412_, double p_108413_, IAnimatedSprite p_108414_) {
		super(p_108407_, p_108408_, p_108409_, p_108410_, 0.0D, 0.0D, 0.0D);
		this.sprites = p_108414_;
		this.lifetime = 16;
		this.setSprite(this.sprites.get(this.random));
		this.hasPhysics = true;
		this.gravity = 1.0F;
		this.xd = p_108411_;
		this.yd = p_108412_;
		this.zd = p_108413_;
	}

	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	public void tick() {
		super.tick();
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		private final IAnimatedSprite sprites;

		public Provider(IAnimatedSprite p_108429_) {
			this.sprites = p_108429_;
		}

		public Particle createParticle(BasicParticleType p_108440_, ClientWorld p_108441_, double p_108442_, double p_108443_, double p_108444_, double p_108445_, double p_108446_, double p_108447_) {
			return new TsunamiSplashParticle(p_108441_, p_108442_, p_108443_, p_108444_, p_108445_, p_108446_, p_108447_, this.sprites);
		}
	}
}