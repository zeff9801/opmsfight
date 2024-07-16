package yesman.epicfight.client.particle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.ClientModels;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;


@OnlyIn(Dist.CLIENT)
public class EntityAfterImageParticle extends CustomModelParticle {
	private final OpenMatrix4f[] poseMatrices;
	private final Matrix4f modelMatrix;
	private float alphaO;

	public EntityAfterImageParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, ClientModel particleMesh, OpenMatrix4f[] matrices, Matrix4f modelMatrix) {
		super(level, x, y, z, xd, yd, zd, particleMesh);
		this.poseMatrices = matrices;
		this.modelMatrix = modelMatrix;
		this.lifetime = 20;
		this.rCol = 1.0F;
		this.gCol = 1.0F;
		this.bCol = 1.0F;
		this.alphaO = 0.3F;
		this.alpha = 0.3F;
	}

	@Override
	public void tick() {
		super.tick();
		this.alphaO = this.alpha;
		this.alpha = (float)(this.lifetime - this.age) / (float)this.lifetime * 0.8F;
	}

	@Override
	public void render(IVertexBuilder vertexConsumer, ActiveRenderInfo camera, float partialTicks) {
		MatrixStack poseStack = new MatrixStack();
		this.setupMatrixStack(poseStack, camera, partialTicks);
		poseStack.last().pose().multiply(this.modelMatrix);
		float alpha = this.alphaO + (this.alpha - this.alphaO) * partialTicks;
		this.particleMesh.drawWithPoseNoTexture(poseStack, vertexConsumer, this.getLightColor(partialTicks), this.rCol, this.gCol, this.bCol, alpha, OverlayTexture.NO_OVERLAY, this.poseMatrices);
	}

	@Override
	public IParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.TRANSLUCENT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Particle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			Entity entity = level.getEntity((int)Double.doubleToLongBits(xSpeed));
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

			if (entitypatch != null && ClientEngine.getInstance().renderEngine.hasRendererFor(entitypatch.getOriginal())) {
				PatchedEntityRenderer renderer = ClientEngine.getInstance().renderEngine.getEntityRenderer(entitypatch.getOriginal());
				Armature armature = entitypatch.getEntityModel(ClientModels.LOGICAL_CLIENT).getArmature();
				MatrixStack poseStack = new MatrixStack();
				OpenMatrix4f[] matrices = renderer.getPoseMatrices(entitypatch, armature, 1.0F);
				renderer.mulPoseStack(poseStack, armature, entitypatch.getOriginal(), entitypatch, 1.0F);

				for (int i = 0; i < matrices.length; i++) {
					matrices[i] = OpenMatrix4f.mul(matrices[i], armature.searchJointById(i).getToOrigin(), null);
				}

				//ClientModel mesh = ClientEngine.getInstance().renderEngine.getEntityRenderer(entitypatch.getOriginal()).getMesh(entitypatch);
				ClientModel mesh = entitypatch.getEntityModel(ClientModels.LOGICAL_CLIENT);
				EntityAfterImageParticle particle = new EntityAfterImageParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, mesh, matrices, poseStack.last().pose());

				return particle;
			}

			return null;
		}
	}
}