package yesman.epicfight.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.ClientModel;
import yesman.epicfight.api.client.model.ClientModels;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class AirBurstParticle extends TexturedCustomModelParticle {

	public static final ResourceLocation AIR_BURST_PARTICLE = new ResourceLocation(EpicFightMod.MODID, "textures/particle/air_burst.png");
	
	public AirBurstParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, ClientModel particleMesh, ResourceLocation texture) {
		super(level, x, y, z, xd, yd, zd, particleMesh, texture);

		this.scale = 0.1F;
		this.scaleO = 0.1F;
		this.lifetime = zd < 0.0D ? 2 : (int)zd;
		this.pitch = (float)xd;
		this.pitchO = (float)xd;
		this.yaw = (float)yd;
		this.yawO = (float)yd;
	}
	
	@Override
	public IParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.PARTICLE_MODEL_NO_NORMAL;
	}
	
	@Override
	public void tick() {
		super.tick();
		this.scale += 0.5F;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		@Override
		public Particle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new AirBurstParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, ClientModels.LOGICAL_CLIENT.laser /*TODO airBurst */, AIR_BURST_PARTICLE);
		}
	}
}