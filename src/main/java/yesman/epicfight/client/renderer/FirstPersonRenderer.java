package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

import java.util.Iterator;

@OnlyIn(Dist.CLIENT)

public class FirstPersonRenderer extends PatchedLivingEntityRenderer<ClientPlayerEntity, LocalPlayerPatch, PlayerModel<ClientPlayerEntity>, LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>>, HumanoidMesh> {

	public FirstPersonRenderer() {
		super();
		this.addPatchedLayer(ElytraLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(HeldItemLayer.class, new PatchedItemInHandLayer<>());
		this.addPatchedLayer(BipedArmorLayer.class, new WearableItemLayer<>(Meshes.BIPED, true));
		this.addPatchedLayer(HeadLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(SpinAttackEffectLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(CapeLayer.class, new EmptyLayer<>());
	}
	
	@Override
	public void render(ClientPlayerEntity entity, LocalPlayerPatch entitypatch, LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight, float partialTicks) {
		Pose pose = entitypatch.getAnimator().getPose(partialTicks);
		OpenMatrix4f[] poses = entitypatch.getArmature().getPoseAsTransformMatrix(pose, false);
		poseStack.pushPose();

		Matrix4f lastPose = new Matrix4f(poseStack.last().pose());
		//poseStack.setIdentity(); TODO

		float correction = 0.0F;

		if (entity.isVisuallySwimming()) {
			correction = 0.25F;
		} else if (entity.isFallFlying()) {
			correction = 100.0F;
		}

		poseStack.translate(0.0F, -entity.getEyeHeight() - 0.05F, correction);
		//poseStack.mulPoseMatrix(lastPose);

		HumanoidMesh mesh = this.getMesh(entitypatch);
		this.prepareModel(mesh, entity, entitypatch, renderer);

		if (!entitypatch.getOriginal().isInvisible()) {
			for (AnimatedMesh.AnimatedModelPart p : mesh.getAllParts()) {
				p.setHidden(true);
			}
			mesh.leftArm.setHidden(false);
			mesh.rightArm.setHidden(false);
			mesh.leftSleeve.setHidden(false);
			mesh.rightSleeve.setHidden(false);

			//RenderType renderType = RenderType.entityCutoutNoCull(entity.getSkinTextureLocation());
			//mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, entitypatch.getArmature(), poses); TODO
			Armature armature = entitypatch.getArmature();
			mesh.drawModelWithPose(poseStack, buffer.getBuffer(EpicFightRenderTypes.animatedModel(entitypatch.getOriginal().getSkinTextureLocation())),
					packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);

		}

		if (!entity.isSpectator()) {
			this.renderLayer(renderer, entitypatch, entity, poses, buffer, poseStack, packedLight, partialTicks);
		}

		poseStack.popPose();
	}

	@Override
	protected void renderLayer(LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer, LocalPlayerPatch entitypatch, ClientPlayerEntity entityIn, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLightIn, float partialTicks) {
		Iterator<LayerRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>>> iter = renderer.layers.iterator();

		float f = MathUtils.lerpBetween(entityIn.yBodyRotO, entityIn.yBodyRot, partialTicks);
		float f1 = MathUtils.lerpBetween(entityIn.yHeadRotO, entityIn.yHeadRot, partialTicks);
		float f2 = f1 - f;
		float f7 = entityIn.getViewXRot(partialTicks);
		float bob = this.getVanillaRendererBob(entityIn, renderer, partialTicks);

		while (iter.hasNext()) {
			LayerRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> layer = iter.next();
			Class<?> rendererClass = layer.getClass();

			if (rendererClass.isAnonymousClass()) {
				rendererClass = rendererClass.getSuperclass();
			}

			if (this.patchedLayers.containsKey(rendererClass)) {
				this.patchedLayers.get(rendererClass).renderLayer(0, entitypatch, entityIn, layer, poseStack, buffer, packedLightIn, poses, bob, f2, f7, partialTicks);
			}
		}
	}

	@Override
	public HumanoidMesh getMesh(LocalPlayerPatch entitypatch) {
		return entitypatch.getOriginal().getModelName().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
	}

	@Override
	protected void prepareModel(HumanoidMesh mesh, ClientPlayerEntity entity, LocalPlayerPatch entitypatch, LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer) {
		mesh.initialize();
		mesh.head.setHidden(true);
		mesh.hat.setHidden(true);
	}
}