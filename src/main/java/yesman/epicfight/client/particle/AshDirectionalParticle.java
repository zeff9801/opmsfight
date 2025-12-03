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
public class AshDirectionalParticle extends SpriteTexturedParticle {
	private final IAnimatedSprite sprites;

	protected AshDirectionalParticle(ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float quadSizeMultiplier, IAnimatedSprite sprites) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.sprites = sprites;
		this.gravity = -0.1F;
		this.lifetime = 20;
		this.quadSize *= quadSizeMultiplier;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
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
		public SpriteTexturedParticle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new AshDirectionalParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 2.0F, this.sprites);
		}
	}
}
