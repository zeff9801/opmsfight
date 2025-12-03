package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.ModelPart;
import yesman.epicfight.api.client.model.VertexIndicator;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.gameasset.Armatures;

import java.util.Map;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)

public class FirstPersonRenderer extends
		PatchedLivingEntityRenderer<ClientPlayerEntity, LocalPlayerPatch, PlayerModel<ClientPlayerEntity>, LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>>, HumanoidMesh> {

	public FirstPersonRenderer() {
		super();
		this.addPatchedLayer(ElytraLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(HeldItemLayer.class, new PatchedItemInHandLayer<>());
		this.addPatchedLayer(BipedArmorLayer.class, new WearableItemLayer<>(Meshes.BIPED, true));
		this.addPatchedLayer(HeadLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(SpinAttackEffectLayer.class, new EmptyLayer<>());
	}

	@Override
	public void render(ClientPlayerEntity entityIn, LocalPlayerPatch entitypatch,
			LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer, IRenderTypeBuffer buffer,
			MatrixStack matStackIn, int packedLightIn, float partialTicks) {
		HumanoidMesh mesh = this.getMesh(entitypatch);
		Pose pose;
		OpenMatrix4f[] poses;
		boolean hasPovSettings = entitypatch.getPovSettings() != null;

		if (hasPovSettings) {
			pose = entitypatch.getFirstPersonLayer().getEnabledPose(entitypatch, true, partialTicks);
		} else {
			pose = entitypatch.getAnimator().getPose(partialTicks);
		}

		poses = entitypatch.getArmature().getPoseAsTransformMatrix(pose);
		matStackIn.pushPose();

		if (hasPovSettings && entitypatch.hasCameraAnimation()) {
			float time = MathHelper.lerp(partialTicks,
					entitypatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(),
					entitypatch.getFirstPersonLayer().animationPlayer.getElapsedTime());
			AnimationSubFileReader.PovSettings settings = entitypatch.getPovSettings();
			JointTransform cameraTransform;

			if (entitypatch.getFirstPersonLayer().animationPlayer.getAnimation().getRealAnimation().isLinkAnimation()
					|| settings == null) {
				cameraTransform = entitypatch.getFirstPersonLayer().getLinkCameraTransform()
						.getInterpolatedTransform(time);
			} else {
				cameraTransform = settings.cameraTransform().getInterpolatedTransform(time);
			}

			MathUtils.mulStack(matStackIn, cameraTransform.toMatrix().invert());
		}

		if (hasPovSettings && entitypatch.getPovSettings() != null) {
			switch (entitypatch.getPovSettings().rootTransformation()) {
				case CAMERA:
					matStackIn.translate(0.0F, -entityIn.getEyeHeight(), 0.0F);
					break;
				case WORLD:
					float yRot = MathUtils.lerpBetween(entityIn.yRotO, entityIn.yRot, partialTicks);
					float xRot = MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);
					matStackIn.mulPose(Vector3f.XP.rotationDegrees(xRot));
					matStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - yRot));
					matStackIn.translate(0.0F, -entityIn.getEyeHeight(), 0.0F);
					break;
			}
		} else {
			OpenMatrix4f mat = entitypatch.getArmature().getBindedTransformFor(pose, Armatures.BIPED.get().head);
			mat.translate(0, 0.2F, 0);
			Vec3f translateVectorOfHead = mat.toTranslationVector();
			matStackIn.translate(-translateVectorOfHead.x, -translateVectorOfHead.y, -translateVectorOfHead.z);
		}

		this.prepareModel(mesh, entityIn, entitypatch, renderer);

		if (!entitypatch.getOriginal().isInvisible()) {
			if (hasPovSettings && entitypatch.getPovSettings() != null) {
				AnimationSubFileReader.PovSettings settings = entitypatch.getPovSettings();
				this.applyVisibilities(mesh, settings.visibilities(), settings.visibilityOthers());
			} else {
				for (ModelPart<VertexIndicator.AnimatedVertexIndicator> p : mesh.getAllParts()) {
					p.hidden = true;
				}

				mesh.leftArm.hidden = false;
				mesh.rightArm.hidden = false;
				mesh.leftSleeve.hidden = false;
				mesh.rightSleeve.hidden = false;

			}

			mesh.drawModelWithPose(matStackIn,
					buffer.getBuffer(
							EpicFightRenderTypes.animatedModel(entitypatch.getOriginal().getSkinTextureLocation())),
					packedLightIn, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, entitypatch.getArmature(), poses);
		}

		if (!entityIn.isSpectator()) {
			renderLayer(renderer, entitypatch, entityIn, poses, buffer, matStackIn, packedLightIn, partialTicks);
		}

		matStackIn.popPose();
	}

	private void applyVisibilities(HumanoidMesh mesh, Map<String, Boolean> visibilities, boolean defaultVisibility) {
		setHidden(mesh.head, visibilities, defaultVisibility, "head");
		setHidden(mesh.torso, visibilities, defaultVisibility, "torso");
		setHidden(mesh.leftArm, visibilities, defaultVisibility, "leftArm");
		setHidden(mesh.rightArm, visibilities, defaultVisibility, "rightArm");
		setHidden(mesh.leftLeg, visibilities, defaultVisibility, "leftLeg");
		setHidden(mesh.rightLeg, visibilities, defaultVisibility, "rightLeg");
		setHidden(mesh.hat, visibilities, defaultVisibility, "hat");
		setHidden(mesh.jacket, visibilities, defaultVisibility, "jacket");
		setHidden(mesh.leftSleeve, visibilities, defaultVisibility, "leftSleeve");
		setHidden(mesh.rightSleeve, visibilities, defaultVisibility, "rightSleeve");
		setHidden(mesh.leftPants, visibilities, defaultVisibility, "leftPants");
		setHidden(mesh.rightPants, visibilities, defaultVisibility, "rightPants");
	}

	private void setHidden(ModelPart<? extends VertexIndicator> part, Map<String, Boolean> visibilities,
			boolean defaultVisibility, String name) {
		part.hidden = !visibilities.getOrDefault(name, defaultVisibility);
	}

	@Override
	protected void renderLayer(LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer,
			LocalPlayerPatch entitypatch, ClientPlayerEntity entityIn, OpenMatrix4f[] poses, IRenderTypeBuffer buffer,
			MatrixStack poseStack, int packedLightIn, float partialTicks) {
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
				this.patchedLayers.get(rendererClass).renderLayer(0, entitypatch, entityIn, layer, poseStack, buffer,
						packedLightIn, poses, bob, f2, f7, partialTicks);
			}
		}
	}

	@Override
	public HumanoidMesh getMesh(LocalPlayerPatch entitypatch) {
		return entitypatch.getOriginal().getModelName().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
	}

	@Override
	protected void prepareModel(HumanoidMesh mesh, ClientPlayerEntity entity, LocalPlayerPatch entitypatch,
			LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer) {
		mesh.initialize();
		mesh.head.hidden = true;
		mesh.hat.hidden = true;
	}
}
