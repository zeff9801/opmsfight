package yesman.epicfight.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatharsisParticle extends SpriteTexturedParticle {
	private final IAnimatedSprite sprites;

	protected CatharsisParticle(ClientWorld level, double x, double y, double z, IAnimatedSprite sprites) {
		super(level, x, y, z);
		this.sprites = sprites;
		this.yd = 0.1D;
		this.quadSize = 0.75F;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		this.alpha -= 0.05F;
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		private final IAnimatedSprite sprites;

		public Provider(IAnimatedSprite sprites) {
			this.sprites = sprites;
		}

		@Override
		public SpriteTexturedParticle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			CatharsisParticle catharsis = new CatharsisParticle(level, x, y, z, this.sprites);
			catharsis.setAlpha(0.8F);
			catharsis.setLifetime(12);
			return catharsis;
		}
	}
}
