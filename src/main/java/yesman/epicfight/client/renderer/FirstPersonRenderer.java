package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.ModelPart;
import yesman.epicfight.api.client.model.VertexIndicator;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Armatures;

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
	}
	
	@Override
	public void render(ClientPlayerEntity entityIn, LocalPlayerPatch entitypatch, LivingRenderer<ClientPlayerEntity, PlayerModel<ClientPlayerEntity>> renderer, IRenderTypeBuffer buffer, MatrixStack matStackIn, int packedLightIn, float partialTicks) {
		Armature armature = entitypatch.getArmature();
		Pose pose = entitypatch.getAnimator().getPose(partialTicks);
		OpenMatrix4f[] poses = armature.getPoseAsTransformMatrix(pose);
		matStackIn.pushPose();
		OpenMatrix4f mat = entitypatch.getArmature().getBindedTransformFor(pose, Armatures.BIPED.head);
		mat.translate(0, 0.2F, 0);

		Vec3f translateVectorOfHead = mat.toTranslationVector();
		matStackIn.translate(-translateVectorOfHead.x, -translateVectorOfHead.y, -translateVectorOfHead.z);
		HumanoidMesh mesh = this.getMesh(entitypatch);
		this.prepareModel(mesh, entityIn, entitypatch, renderer);

		if (!entitypatch.getOriginal().isInvisible()) {
			for (ModelPart<VertexIndicator.AnimatedVertexIndicator> p : mesh.getAllParts()) {
				p.hidden = true;
			}

			mesh.leftArm.hidden = false;
			mesh.rightArm.hidden = false;
			mesh.leftSleeve.hidden = false;
			mesh.rightSleeve.hidden = false;

			mesh.drawModelWithPose(matStackIn, buffer.getBuffer(EpicFightRenderTypes.animatedModel(entitypatch.getOriginal().getSkinTextureLocation())),
					packedLightIn, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
		}

		if (!entityIn.isSpectator()) {
			renderLayer(renderer, entitypatch, entityIn, poses, buffer, matStackIn, packedLightIn, partialTicks);
		}

		matStackIn.popPose();
		
		if(!entityIn.isSpectator()) {
			renderLayer(renderer, entitypatch, entityIn, poses, buffer, matStackIn, packedLightIn, partialTicks);
		}
		
		matStackIn.popPose();
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
		mesh.head.hidden = true;
		mesh.hat.hidden = true;
	}
}